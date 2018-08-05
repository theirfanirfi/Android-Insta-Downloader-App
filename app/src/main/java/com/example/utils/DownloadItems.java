package com.example.utils;

import java.io.Serializable;

public class DownloadItems implements Serializable {

    String name;
    String by;
    String image;
    String video;
    String type;
    String temp_url;

    public DownloadItems(String name, String by, String image, String video, String type, String temp_url)
    {
        this.name = name;
        this.by = by;
        this.image = image;
        this.video = video;
        this.type = type;
        this.temp_url = temp_url;
    }

    public String getName()
    {
        return name;
    }

    public String getBy()
    {
        return by;
    }

    public String getImage()
    {
        return image;
    }

    public String getVideo()
    {
        return video;
    }

    public String getType()
    {
        return type;
    }

    public String getTemp_url()
    {
        return temp_url;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setBy(String by)
    {
        this.by = by;
    }

    public void setImage(String image)
    {
        this.image = image;
    }

    public void setVideo(String video)
    {
        this.video = video;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public void setTemp_url(String temp_url)
    {
        this.temp_url = temp_url;
    }
}
