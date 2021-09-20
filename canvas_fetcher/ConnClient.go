package main

import (
	"fmt"
	"io"
	"io/ioutil"
	"log"
	"net/http"
	"os"
	"strings"
)

type ConnClient struct {
	API_ENDPOINT string
	TOKEN        string
	DESTINATION  string
}

func (client *ConnClient) download_file(file_url, file_name string) {
	http_client := &http.Client{}
	log.Printf("URL: %s\n", file_url)
	req, _ := http.NewRequest("GET", file_url, nil)
	req.Header.Set("Authorization", fmt.Sprintf("Bearer %s", client.TOKEN))
	response, err := http_client.Do(req)
	if err != nil {
		log.Fatal(fmt.Sprintf("Error: %s\n", err))
	}
	defer response.Body.Close()
	out_file, err := os.Create(strings.Join([]string{client.DESTINATION, file_name}, "/"))
	if err != nil {
		log.Fatal(fmt.Sprintf("Error: %s\n", err))
	}
	defer out_file.Close()
	n, err := io.Copy(out_file, response.Body)
	if err != nil {
		log.Fatal(fmt.Sprintf("Error: %s\n", err))
	}
	log.Printf("Written %d byte(s) to file\n", n)
}

func (client *ConnClient) new_request(context_type, context_id, resource_type, resources_id string) []byte {
	http_client := &http.Client{}
	var args []string
	for _, value := range []string{client.API_ENDPOINT, context_type, context_id, resource_type, resources_id} {
		if value != "" {
			args = append(args, value)
		}
	}
	request_url := strings.Join(args, "/")
	log.Printf("URL: %s\n", request_url)
	req, _ := http.NewRequest("GET", request_url, nil)
	req.Header.Set("Authorization", fmt.Sprintf("Bearer %s", client.TOKEN))
	req.Header.Set("Content-type", "application/json")
	req.Header.Add("enrollment_state", "active")
	q := req.URL.Query()
	// send as parameter instead of header
	q.Add("per_page", "32767")
	req.URL.RawQuery = q.Encode()
	fmt.Printf("req: %v\n", req)

	response, err := http_client.Do(req)
	if err != nil {
		log.Fatal(fmt.Sprintf("Error: %s\n", err))
	}
	defer response.Body.Close()
	response_body, err := ioutil.ReadAll(response.Body)
	if err != nil {
		log.Fatal(fmt.Sprintf("Error: %s\n", err))
	}
	return response_body
}
