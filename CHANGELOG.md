# Changelog

## [0.3.0](https://github.com/gabia/podman-java-sdk/compare/v0.2.0...v0.3.0) (2026-10-30)


### Features

* add system events subscription ([#35](https://github.com/gabia/podman-java-sdk/issues/35)) ([4480a4c](https://github.com/gabia/podman-java-sdk/commit/4480a4c5ed7ea2104582c322c600dcfd63e934fd))


### Bug Fixes

* stop enforcing the request deadline on streamed bodies ([#34](https://github.com/gabia/podman-java-sdk/issues/34)) ([93f0c6a](https://github.com/gabia/podman-java-sdk/commit/93f0c6a34be8b0176bc5c870751fcb0d459e24c3))


### Dependencies

* bump com.fasterxml.jackson.core:jackson-databind ([#30](https://github.com/gabia/podman-java-sdk/issues/30)) ([4269d28](https://github.com/gabia/podman-java-sdk/commit/4269d28e7a82a05a6073c9ef0c3cfbefd1cdd3a0))
* bump com.fasterxml.jackson.datatype:jackson-datatype-jsr310 ([#32](https://github.com/gabia/podman-java-sdk/issues/32)) ([7d7cea7](https://github.com/gabia/podman-java-sdk/commit/7d7cea7930b36b21c661b8776b6f57e2cf21f19d))
* bump gradle-wrapper from 9.8.1 to 9.8.2 ([#29](https://github.com/gabia/podman-java-sdk/issues/29)) ([1992b35](https://github.com/gabia/podman-java-sdk/commit/1992b35579edaa4924700018eb61bdd525d2597f))
* bump org.springframework.boot:spring-boot-dependencies ([#33](https://github.com/gabia/podman-java-sdk/issues/33)) ([c89253f](https://github.com/gabia/podman-java-sdk/commit/c89253fd85c2c4bb5fb047f96188bfefecdd3cdf))
* bump tools.jackson.core:jackson-databind from 3.2.3 to 3.2.4 ([#31](https://github.com/gabia/podman-java-sdk/issues/31)) ([ae4f4da](https://github.com/gabia/podman-java-sdk/commit/ae4f4da7705bb52adc1ccfdf5a88ba43949d94ff))


### Documentation

* add README requirements and starter examples ([#27](https://github.com/gabia/podman-java-sdk/issues/27)) ([25d728f](https://github.com/gabia/podman-java-sdk/commit/25d728f3c02cbc738c9a108bdcfbd9b82ba1160f))

## [0.2.0](https://github.com/gabia/podman-java-sdk/compare/v0.1.0...v0.2.0) (2026-10-22)


### ⚠ BREAKING CHANGES

* split Jackson 2 and Jackson 3 support into separate modules ([#17](https://github.com/gabia/podman-java-sdk/issues/17))

### Features

* add container create, start and wait ([#20](https://github.com/gabia/podman-java-sdk/issues/20)) ([849d560](https://github.com/gabia/podman-java-sdk/commit/849d560976e90b21f805c98fe3c67ad327d65236))
* add podman-java-sdk-bom module ([#21](https://github.com/gabia/podman-java-sdk/issues/21)) ([6e6d50d](https://github.com/gabia/podman-java-sdk/commit/6e6d50deaf9d800914d9eff8a99cad105b4e19ab))
* add podman-java-sdk-spring-boot4-starter module ([#24](https://github.com/gabia/podman-java-sdk/issues/24)) ([db7b304](https://github.com/gabia/podman-java-sdk/commit/db7b30433c1663180a5e1642b6629a57d201980b))
* split Jackson 2 and Jackson 3 support into separate modules ([#17](https://github.com/gabia/podman-java-sdk/issues/17)) ([b3c8c79](https://github.com/gabia/podman-java-sdk/commit/b3c8c79de015d428da49a92aaa34caf393da5f56))


### Dependencies

* bump com.diffplug.spotless:com.diffplug.spotless.gradle.plugin ([#12](https://github.com/gabia/podman-java-sdk/issues/12)) ([979e03b](https://github.com/gabia/podman-java-sdk/commit/979e03b7924db4821d0b5bbdd1124a0fef604fd3))
* bump gradle-wrapper from 9.8.0 to 9.8.1 ([#13](https://github.com/gabia/podman-java-sdk/issues/13)) ([a4dea10](https://github.com/gabia/podman-java-sdk/commit/a4dea108ba096823fec1fdbfe3bd5674c31b04ed))
* bump io.freefair.gradle:lombok-plugin from 9.8.0 to 9.8.2 ([#18](https://github.com/gabia/podman-java-sdk/issues/18)) ([b53230f](https://github.com/gabia/podman-java-sdk/commit/b53230f05a80c6335e5dccad26e957cab1b2b27d))

## 0.1.0 (2026-10-08)


### Features

* add Podman REST API client ([73f9482](https://github.com/gabia/podman-java-sdk/commit/73f94827885e35dcdaf1a6ac1cbba12665a09d78))


### Dependencies

* bump com.diffplug.spotless:com.diffplug.spotless.gradle.plugin ([#6](https://github.com/gabia/podman-java-sdk/issues/6)) ([ebadfa5](https://github.com/gabia/podman-java-sdk/commit/ebadfa59b2bd8626cc142027cdc1a0fcb87a9082))
* bump com.fasterxml.jackson.core:jackson-databind ([#5](https://github.com/gabia/podman-java-sdk/issues/5)) ([7588f8a](https://github.com/gabia/podman-java-sdk/commit/7588f8a2b267178553e1496980c7d4daa419a2e8))
* bump gradle-wrapper from 9.7.1 to 9.8.0 ([#4](https://github.com/gabia/podman-java-sdk/issues/4)) ([ad7331e](https://github.com/gabia/podman-java-sdk/commit/ad7331e8a7a7d8a68c7dec01188979b043b42cf0))
* bump io.freefair.gradle:lombok-plugin from 9.5.0 to 9.8.0 ([#7](https://github.com/gabia/podman-java-sdk/issues/7)) ([b675395](https://github.com/gabia/podman-java-sdk/commit/b675395ebda48148aee3d4f8738fad443589896c))
