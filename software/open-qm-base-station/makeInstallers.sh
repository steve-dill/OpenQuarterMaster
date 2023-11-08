#!/bin/bash
#
# Script to make installers for OQM- Base Station.
#
# Intended to be run from the dir this resides
#
# Author: Greg Stewart
#
# Requires packages:
#  Ubuntu:
#   - dpkg-dev
#   - rpm
#   - rpmlint
#   - jq
#
# TODO:: Figure out how logs work

srcDir="installerSrc"
configFile="$srcDir/installerProperties.json"
buildDir="build/installers"

debDir="StationCaptainDeb"

#
# Clean
#

rm -rf "$buildDir"
#
# Setup
#

mkdir -p "$buildDir"

#
# Debian build
#

mkdir "$buildDir/$debDir"
mkdir "$buildDir/$debDir/DEBIAN"
mkdir -p "$buildDir/$debDir/etc/systemd/system/"
mkdir -p "$buildDir/$debDir/etc/oqm/serviceConfig/core/base+station/"
mkdir -p "$buildDir/$debDir/etc/oqm/config/configs/"
mkdir -p "$buildDir/$debDir/etc/oqm/proxyConfig.d/"
mkdir -p "$buildDir/$debDir/usr/share/applications"

install -m 755 -D "$srcDir/base-station-config.list" "$buildDir/$debDir/etc/oqm/serviceConfig/core/base+station/"
install -m 755 -D "$srcDir/20-baseStation.json" "$buildDir/$debDir/etc/oqm/config/configs/"
install -m 755 -D "$srcDir/oqm-base-station.desktop" "$buildDir/$debDir/usr/share/applications/"
install -m 755 -D "$srcDir/core-baseStation-proxy-config.json" "$buildDir/$debDir/etc/oqm/proxyConfig.d/"

serviceFile="open+quarter+master-core-base+station.service"
serviceFileEscaped="$(systemd-escape "$serviceFile")"

cp "$srcDir/$serviceFile" "$buildDir/$debDir/etc/systemd/system/$serviceFileEscaped"
sed -i "s/\${version}/$(./gradlew -q printVersion)/" "$buildDir/$debDir/etc/systemd/system/$serviceFileEscaped"

# TODO:: license information https://www.debian.org/doc/packaging-manuals/copyright-format/1.0/
# https://www.debian.org/doc/debian-policy/ch-controlfields.html#s-binarycontrolfiles
cat <<EOT >> "$buildDir/$debDir/DEBIAN/control"
Package: $(cat "$configFile" | jq -r '.packageName')
Version: $(./gradlew -q printVersion)
Section: Open QuarterMaster
Maintainer: $(cat "$configFile" | jq -r '.maintainer.name')
Architecture: all
Description: $(cat "$configFile" | jq -r '.description')
Homepage: $(cat "$configFile" | jq -r '.homepage')
Depends: $(cat "$configFile" | jq -r '.dependencies.deb')
Licence: $(cat "$configFile" | jq -r '.copyright.licence')
EOT

cat <<EOT >> "$buildDir/$debDir/DEBIAN/copyright"
Format: https://www.debian.org/doc/packaging-manuals/copyright-format/1.0/
Upstream-Name: Open QuarterMaster Base Station
Upstream-Contact: $(cat "$configFile" | jq -r '.copyright.contact')
Source: $(cat "$configFile" | jq -r '.homepage')

Files: *
Copyright: $(cat "$configFile" | jq -r '.copyright.copyright')
License: $(cat "$configFile" | jq -r '.copyright.licence')
EOT


cat <<EOT >> "$buildDir/$debDir/DEBIAN/preinst"
#!/bin/bash

mkdir -p /etc/oqm/serviceConfig/core/base+station/files/

# https://unix.stackexchange.com/questions/104171/create-ssl-certificate-non-interactively
if [ ! -f "/etc/oqm/serviceConfig/core/base+station/files/https-cert-cert.pem" ]; then
	echo "Setting up keys."
	openssl req -newkey rsa:2048 -new -nodes -x509 -days 3650 \
		-keyout /etc/oqm/serviceConfig/core/base+station/files/https-cert-key.pem \
		-out /etc/oqm/serviceConfig/core/base+station/files/https-cert-cert.pem \
		-subj "/C=US/ST=Denial/L=Springfield/O=OQM/CN=$(hostname).local"

	openssl req -newkey rsa:2048 -new -nodes -x509 -days 3650 \
		-keyout /etc/oqm/serviceConfig/core/base+station/files/jwt-cert-key.pem \
		-out /etc/oqm/serviceConfig/core/base+station/files/jwt-cert-cert.pem \
		-subj "/C=US/ST=Denial/L=Springfield/O=OQM/CN=$(hostname).local"

	chmod 644 /etc/oqm/serviceConfig/core/base+station/files/https-cert-key.pem
	chmod 644 /etc/oqm/serviceConfig/core/base+station/files/jwt-cert-key.pem
	# TODO:: finish/test/see if works with jwt
fi

if [ ! -f "/etc/oqm/serviceConfig/core/base+station/user-config.list" ]; then
	cat <<EOF >> "/etc/oqm/serviceConfig/core/base+station/user-config.list"
# Add your own config here.
# Configuration here will override those in base-station-config.list
# Reference: https://github.com/Epic-Breakfast-Productions/OpenQuarterMaster/blob/main/software/open-qm-base-station/docs/BuildingAndDeployment.adoc


EOF
fi
EOT
chmod +x "$buildDir/$debDir/DEBIAN/preinst"

cat <<EOT >> "$buildDir/$debDir/DEBIAN/postinst"
#!/bin/bash

systemctl daemon-reload
# restart proxy after we add config
systemctl restart "open\\x2bquarter\\x2bmaster\\x2dinfra\\x2dnginx.service"
systemctl enable "$serviceFileEscaped"
systemctl start "$serviceFileEscaped"
EOT
chmod +x "$buildDir/$debDir/DEBIAN/postinst"

cat <<EOT >> "$buildDir/$debDir/DEBIAN/prerm"
#!/bin/bash

systemctl disable "$serviceFileEscaped"
systemctl stop "$serviceFileEscaped"
EOT
chmod +x "$buildDir/$debDir/DEBIAN/prerm"

cat <<EOT >> "$buildDir/$debDir/DEBIAN/postrm"
#!/bin/bash

systemctl daemon-reload
# Remove docker image
if [[ "$(docker images -q oqm_base_station 2> /dev/null)" != "" ]]; then
        docker rmi oqm_base_station
        echo "Removed docker image."
else
        echo "Docker image was already gone."
fi
if [ $( docker ps -a | grep oqm_base_station | wc -l ) -gt 0 ]; then
        docker rm oqm_base_station
        echo "Removed docker container."
else
        echo "Docker container was already gone."
fi
systemctl restart "open\\x2bquarter\\x2bmaster\\x2dinfra\\x2dnginx.service"

EOT
chmod +x "$buildDir/$debDir/DEBIAN/postrm"

dpkg-deb --build "$buildDir/$debDir" "$buildDir"



#
# RPM build
#


