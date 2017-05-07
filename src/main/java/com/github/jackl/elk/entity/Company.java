package com.github.jackl.elk.entity;

/**
 * Created by jackl on 2017/5/7.
 */
public class Company {

    private static final long serialVersionUID = 1L;

    //ID
    private Long id;
    private String name;
    private String website;

    public Company(Long id, String name, String website) {
        this.id = id;
        this.name = name;
        this.website = website;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }
}
