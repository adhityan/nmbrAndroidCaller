package nmbr.merchant.caller.libs.api;

public class APIResponse {
    public String result;
    public int status;

    public APIResponse(String result, int status) {
        this.result = result;
        this.status = status;
    }
}