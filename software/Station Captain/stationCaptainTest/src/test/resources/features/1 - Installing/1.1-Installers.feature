@feature
@feature-1
@feature-1.1
Feature: 1.1 - Installers

  This feature covers the creation of installers.

  Scenario: Installers are made
    When the command to make the installers are made
    Then command returns successfully
    And the following installers were created:
      | deb |
#      | rpm |
