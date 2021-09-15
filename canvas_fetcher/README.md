# fetcher

## Qt-binding

### installation

* for Qt 5.12.x, set env var "QT_API" to "5.12.0"; similarly, for Qt 5.13.x, set it to 5.13.0.
* set env var "QT_VERSION" to exact version of Qt installed, "qtsetup" use it to search for installation path of Qt
* If any package cannot be found with `go install`, try with `go get -d xxx` to download each package one by one
* use `set GO111MODULE=on` if the binding is to be installed for single module (per project).
* for API 5.13.0, deprecated `QScript` is required. Missing this will lead to failure in installing the binding with `qtsetup`

### setup for usage

* run qtenv.bat to setup env for Qt before each time of using the binding
* On Windows 10, only cmd is supported, no powershell

