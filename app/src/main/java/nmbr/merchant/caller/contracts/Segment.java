package nmbr.merchant.caller.contracts;

import org.json.JSONObject;

/**
 * Created by adhityan on 28/01/15.
 */
public class Segment {
    public int id;
    public String name;
    public String color;
    public String description;

    public int mapcount;

    public Segment(int id, String name, String color, String description) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.description = description;
        this.mapcount = 0;
    }

    public Segment(JSONObject segment) {
        try {
            this.id = segment.getInt("id");
            this.name = segment.getString("name");
            this.color = segment.getString("color");
            this.description = segment.getString("description");
            this.mapcount = segment.getInt("map_count");
        } catch (Exception ignored) { }
    }
}
