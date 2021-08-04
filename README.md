# Elemental2 + Akasha WebGPU + J2CL

An experiment to combine Elemental2 + Akasha WebGPU in J2CL application. Build via:

        $ ./mvnw clean package

Instructions on how this was built so can recreate this in the future:

* Build the new webgpu-j2cl target in akasha via `buildr akasha:webgpu-j2cl:install GWT=no TEST=no PRODUCT_VERSION=0.18` and this will install the artifacts in the local repository
* Copy artifacts installed in the repository to local per-project dir for posterity `cp -R ~/.m2/repository/org/realityforge/akasha/akasha-webgpu-j2cl/ .../elemental2-webgpu-example/repository/org/realityforge/akasha/`
* Copy `akasha_patches.extern.js` from akasha to `src/main/java/org/realityforge/webgpu/akasha_patches.extern.js` and remove some externs not needed. Most of these should disappear in a future version of closure compiler. 