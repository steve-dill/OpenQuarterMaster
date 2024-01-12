<?php

// autoload_static.php @generated by Composer

namespace Composer\Autoload;

class ComposerStaticInit553cfb7e2d21df646689160a99599f70
{
    public static $prefixLengthsPsr4 = array (
        'E' => 
        array (
            'Ebprod\\OqmCoreDepot\\' => 20,
        ),
    );

    public static $prefixDirsPsr4 = array (
        'Ebprod\\OqmCoreDepot\\' => 
        array (
            0 => __DIR__ . '/../..' . '/res/script',
        ),
    );

    public static $classMap = array (
        'Composer\\InstalledVersions' => __DIR__ . '/..' . '/composer/InstalledVersions.php',
    );

    public static function getInitializer(ClassLoader $loader)
    {
        return \Closure::bind(function () use ($loader) {
            $loader->prefixLengthsPsr4 = ComposerStaticInit553cfb7e2d21df646689160a99599f70::$prefixLengthsPsr4;
            $loader->prefixDirsPsr4 = ComposerStaticInit553cfb7e2d21df646689160a99599f70::$prefixDirsPsr4;
            $loader->classMap = ComposerStaticInit553cfb7e2d21df646689160a99599f70::$classMap;

        }, null, ClassLoader::class);
    }
}
