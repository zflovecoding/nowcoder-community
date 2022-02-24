package com.nowcoder.community.entity;
/*
Encapsulates paging related messages
*/
public class Page {
    //current　page number
    //here current must be initialized ,
    // or getTo()...method,which uses current values,will can't get value
    //the same with limit
    //Remember to initalize
    private int current = 1;
    //display limit of current page
    private int limit = 10;
    //display the total number of data
    private int rows;
    //display the query path
    private String path;

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        if(current>=1){
            this.current = current;
        }

    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        if(limit>=1&& limit<100){
            this.limit = limit;
        }

    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        if(rows>=0){
            this.rows = rows;
        }

    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
    //start line of each page
    public int getOffset(){
        return (current-1)*limit;
    }
    //get total pages
    public int getTotal(){
        if(rows%limit==0){
            return rows / limit;
        }else{
            return rows / limit +1;
        }
    }
    /**
     * 获取起始页码
     *
     * @return
     */
    public int getFrom() {
        int from = current - 2;
        return from < 1 ? 1 : from;
    }

    /**
     * 获取结束页码
     *
     * @return
     */
    public int getTo() {
        int to = current + 2;
        int total = getTotal();
        return to > total ? total : to;
    }

}
