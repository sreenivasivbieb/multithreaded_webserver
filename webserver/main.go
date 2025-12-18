package main



import(
	"fmt"
	"os"
)
type cacheElement struct{

	data string
	len int
	url string
	next *cacheElement
    prev *cacheElement
}
type LinkedList struct{
	head *cacheElement
}

var sem = make(chan struct{}, 3) // allow max 3 clients

func (l *LinkedList)find(url string) *cacheElement{
	curr:=l.head
	for (curr!=nil){
		if (curr.url==url){
			return curr
		}
	}
	return nil
}

func (l *LinkedList)remove_cache_element(){
   
}
var port int=8888;
var 


func  main(){
     
}