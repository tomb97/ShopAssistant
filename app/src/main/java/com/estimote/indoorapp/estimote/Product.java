package com.estimote.indoorapp.estimote;

/**
 * Created by tombarrett on 31/07/2017.
 */

public class Product {

    private String name;
    private String summary;
    private String template;

    public Product(String name, String summary, String template) {
        this.name = name;
        this.summary = summary;
        this.template=template;
    }

    public String getName() {
        return name;
    }

    public String getSummary() {
        return summary;
    }

    public String getTemplate() { return template; }
}
