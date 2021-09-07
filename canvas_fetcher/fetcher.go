package main

import (
	"encoding/json"
	"fmt"
	"io/ioutil"
	"net/http"

	"github.com/spf13/viper"
)

func getData(url string, token string) {
	client := &http.Client{}
	req, _ := http.NewRequest("GET", url, nil)
	req.Header.Set("Authorization", fmt.Sprintf("Bearer %s", token))
	req.Header.Set("Content-type", "application/json")
	req.Header.Set("enrollment_state", "active")
	req.Header.Set("per_page", "32767")
	resp, err := client.Do(req)

	if err != nil {
		fmt.Println(err)
		return
	}
	defer resp.Body.Close()

	//read body as bytes rom reader
	body, _ := ioutil.ReadAll(resp.Body)

	// save output for study if needed
	// os.WriteFile("record.json", body, 0644)

	// list of json object, each obj is map of string to interface(actually string)
	var itemList []map[string]interface{}

	// read from bytes
	json.Unmarshal(body, &itemList)

	// each item of itemList is a map, access value with key directly
	for _, item := range itemList {
		fmt.Printf("course_code: %s\n", item["course_code"])
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

func main() {
	CFG := readConfig()
	var resource_type = "courses"
	// TODO: find better way to handle url concat
	url := fmt.Sprintf("%s/%s", CFG["API_ENDPOINT"], resource_type)
	getData(url, CFG["TOKEN"])
}
