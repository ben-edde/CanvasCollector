import QtQuick 2.12
import QtQuick.Window 2.12

Window {
    visible: true
    width: 640
    height: 480
    title: qsTr("Hello World")

    Text {
        id: element
        x: 136
        y: 153
        text: qsTr("ultimate answer to life, universe, and everything")
        font.pixelSize: 12
    }
}
