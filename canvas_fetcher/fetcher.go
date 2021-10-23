package main

import (
	"bufio"
	"encoding/json"
	"flag"
	"fmt"
	"log"
	"os"
	"path"
	"path/filepath"
	"strconv"
	"strings"

	"github.com/boltdb/bolt"
	"github.com/spf13/viper"
	// "github.com/therecipe/qt/core"
	// "github.com/therecipe/qt/gui"
	// "github.com/therecipe/qt/qml"
)

type Fetcher struct {
	db         *bolt.DB
	con_client *ConnClient
	cfg        map[string]string
}

func (fetcher *Fetcher) getData(context string, context_id string, resource_type string, resources_id string) []map[string]interface{} {

	response_body := fetcher.con_client.new_request(context, context_id, resource_type, resources_id)

	//os.WriteFile("record.json", response_body, 0644)

	// list of json object, each obj is map of string to interface(actually string)
	var itemList []map[string]interface{}

	// read from bytes
	json.Unmarshal(response_body, &itemList)

	return itemList
}

func accept_choices() []int {
	reader := bufio.NewReader(os.Stdin)
	fmt.Print("Enter choices: ")
	raw_input, _ := reader.ReadString('\n')
	raw_input = strings.TrimSpace(raw_input)
	inputs := strings.Split(raw_input, " ")
	choices := []int{}
	for _, ele := range inputs {
		idx, err := strconv.Atoi(ele)
		if err != nil {
			fmt.Printf("error: %s", err)
		}
		choices = append(choices, idx)
		// 	fmt.Printf("%d: %s\n", idx, itemList[idx]["course_code"])
	}
	return choices
}

func (fetcher *Fetcher) course_handler(course map[string]interface{}, parent_path string) {

	course_path := ""
	course_name := fmt.Sprintf("%v", course["name"])
	if parent_path == "" {
		course_path = course_name
	} else {
		course_path = filepath.Join(parent_path, course_name)
	}
	// fmt.Println(course_path)
	err := os.MkdirAll(course_path, os.ModePerm)
	if err != nil {
		fmt.Println(err)
		return
	}

	foldersList := fetcher.getData("courses", fmt.Sprintf("%v", course["id"]), "folders", "")

	// show_result(foldersList, "full_name")
	for _, folder := range foldersList {
		fetcher.folder_handler(folder, course_path)
	}
}

func (fetcher *Fetcher) folder_handler(folder map[string]interface{}, parent_path string) {
	folder_path := ""
	folder_name := fmt.Sprintf("%v", folder["name"])
	if parent_path == "" {
		folder_path = folder_name
	} else {
		folder_path = filepath.Join(parent_path, folder_name)
	}
	// fmt.Println(folder_path)
	err := os.MkdirAll(folder_path, os.ModePerm)
	if err != nil {
		fmt.Println(err)
		return
	}

	response_body := fetcher.con_client.new_request_url(fmt.Sprintf("%v", folder["files_url"]))
	var filesList []map[string]interface{}

	// read from bytes
	json.Unmarshal(response_body, &filesList)

	// show_result(filesList, "filename")
	for _, file := range filesList {
		url := fmt.Sprintf("%v", file["url"])
		filename := fmt.Sprintf("%v", file["filename"])
		// file_path := filepath.Join(folder_path, filename)
		// if _, err := os.Stat(file_path); err == nil {
		// 	fmt.Printf("file %s existed in %s", filename, folder_path)
		file_existed := false
		fetcher.db.Update(func(tx *bolt.Tx) error {
			bucket, err := tx.CreateBucketIfNotExists([]byte("file_md5"))
			if err != nil {
				log.Fatal("Error: ", err)
			}
			file_md5 := bucket.Get([]byte(filename))
			file_existed = file_md5 != nil
			return nil
		})
		if !file_existed {
			file_md5 := fetcher.con_client.download_file(url, filename, folder_path)
			fmt.Printf("downloaded: %s/%s\n", folder_path, filename)
			fetcher.db.Update(func(tx *bolt.Tx) error {
				bucket, err := tx.CreateBucketIfNotExists([]byte("file_md5"))
				if err != nil {
					log.Fatal("Error: %s", err)
				}
				err = bucket.Put([]byte(filename), file_md5)
				if err != nil {
					log.Fatal("Error: %s", err)
				}
				return err
			})
		}
	}
}

func show_result(itemList []map[string]interface{}, column string) {
	// each item of itemList is a map, access value with key directly
	for idx, item := range itemList {
		fmt.Printf("[%d] item:  %s\n", idx, item[column])
	}
}

// read config from file
func readConfig(cfg_path *string) map[string]string {
	CFG := make(map[string]string)
	cfg_dir, _ := path.Split(*cfg_path)
	cfg_file := strings.ReplaceAll(path.Base(*cfg_path), path.Ext(*cfg_path), "")
	viper.SetConfigName(cfg_file)
	viper.AddConfigPath(cfg_dir)
	// dir must be in /
	err := viper.ReadInConfig()
	if err != nil {
		panic(fmt.Errorf("fatal error config file: %w", err))
	}
	CFG["API_ENDPOINT"] = viper.GetString("API_ENDPOINT")
	CFG["TOKEN"] = viper.GetString("TOKEN")
	CFG["DESTINATION"] = viper.GetString("DESTINATION")
	return CFG
}

func set_log(log_path *string) *os.File {
	f, err := os.OpenFile(*log_path, os.O_RDWR|os.O_CREATE|os.O_APPEND, 0666)
	if err != nil {
		log.Fatalf("error opening file: %v", err)
	}
	log.SetOutput(f)
	log.SetFlags(log.LstdFlags | log.Lshortfile)
	return f
}

func mode_gui() {
	// core.QCoreApplication_SetAttribute(core.Qt__AA_EnableHighDpiScaling, true)

	// gui.NewQGuiApplication(len(os.Args), os.Args)

	// var app = qml.NewQQmlApplicationEngine(nil)
	// app.Load(core.NewQUrl3("main.qml", 0))

	// gui.QGuiApplication_Exec()
}

func mode_default(CFG map[string]string) {
	//download all by default
	conn_clint := ConnClient{CFG["API_ENDPOINT"], CFG["TOKEN"], CFG["DESTINATION"]}
	fetcher_db, err := bolt.Open("fetcher.db", 0600, nil)
	if err != nil {
		fmt.Println(err)
	}
	fetcher := Fetcher{fetcher_db, &conn_clint, CFG}

	coursesList := fetcher.getData("courses", "", "", "")
	// show_result(coursesList, "course_code")
	// chosen_index := accept_choices()

	for _, course := range coursesList {
		fetcher.course_handler(course, CFG["DESTINATION"])
	}
}

func check_path_exists(path_str string) (bool, error) {
	dir, _ := path.Split(path_str)
	if dir == "" {
		dir, _ = os.Getwd()
	}
	_, err := os.Stat(dir)
	if err == nil {
		return true, nil
	}
	if os.IsNotExist(err) {
		return false, nil
	}
	return false, err
}

func main() {
	cfg_path := flag.String("cfg", "cfg/config.yaml", "path to yaml config file")
	log_path := flag.String("log", "fetcher.log", "path to log file")
	flag.Parse()
	exist, err := check_path_exists(*cfg_path)
	if err != nil || !exist {
		fmt.Printf("Config directory path (%s) exists: %v\nError: %v\n", *cfg_path, exist, err)
		return
	}
	exist, err = check_path_exists(*log_path)
	if err != nil || !exist {
		fmt.Printf("Log path (%s) exists: %v\nError: %v\n", *log_path, exist, err)
		return
	}
	log_file := set_log(log_path)
	CFG := readConfig(cfg_path)
	defer log_file.Close()
	mode_default(CFG)
}
