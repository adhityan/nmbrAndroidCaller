package nmbr.merchant.caller.contracts;

import org.json.JSONObject;

public class SegmentMap {
    public Segment segment;
    public boolean isForced;

    public SegmentMap(boolean isForced, Segment segment) {
        this.segment = segment;
        this.isForced = isForced;
    }

    public SegmentMap(JSONObject map) {
        try {
            this.segment = new Segment(map.getJSONObject("segment"));
            this.isForced = map.getBoolean("forced");
        } catch (Exception ignored) { }
    }
}