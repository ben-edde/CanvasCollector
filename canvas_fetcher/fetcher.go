package main

import (
	"encoding/json"
	"fmt"
	"log"
	"os"

	"github.com/spf13/viper"
	"github.com/therecipe/qt/core"
	"github.com/therecipe/qt/gui"
	"github.com/therecipe/qt/qml"
)

func getData(cclient ConnClient) {
	response_body := cclient.new_request("courses", "", "", "")
	//cclient.download_file(file_url)
	show_result(response_body)
}

func show_result(response_body []byte) {
	//os.WriteFile("record.json", response_body, 0644)

	// list of json object, each obj is map of string to interface(actually string)
	var itemList []map[string]interface{}

	// read from bytes
	json.Unmarshal(response_body, &itemList)

	// each item of itemList is a map, access value with key directly
	for _, item := range itemList {
		fmt.Printf("item: %s\n", item)
	}
}

// read config from file
func readConfig() map[string]string {
	CFG := make(map[string]string)
	viper.SetConfigName("config")
	viper.AddConfigPath("cfg")
	err := viper.ReadInConfig()
	if err != nil {
		panic(fmt.Errorf("fatal error config file: %w", err))
	}
	CFG["API_ENDPOINT"] = viper.GetString("API_ENDPOINT")
	CFG["TOKEN"] = viper.GetString("TOKEN")
	CFG["DESTINATION"] = viper.GetString("DESTINATION")
	return CFG
}

func set_log() *os.File {
	f, err := os.OpenFile("testing.log", os.O_RDWR|os.O_CREATE|os.O_APPEND, 0666)
	if err != nil {
		log.Fatalf("error opening file: %v", err)
	}
	log.SetOutput(f)
	log.SetFlags(log.LstdFlags | log.Lshortfile)
	return f
}

func main() {
	// log_file := set_log()
	// CFG := readConfig()
	// conn_clint := ConnClient{CFG["API_ENDPOINT"], CFG["TOKEN"], CFG["DESTINATION"]}
	// getData(conn_clint)
	// defer log_file.Close()
	core.QCoreApplication_SetAttribute(core.Qt__AA_EnableHighDpiScaling, true)

	gui.NewQGuiApplication(len(os.Args), os.Args)

	var app = qml.NewQQmlApplicationEngine(nil)
	app.Load(core.NewQUrl3("main.qml", 0))

	gui.QGuiApplication_Exec()

}
