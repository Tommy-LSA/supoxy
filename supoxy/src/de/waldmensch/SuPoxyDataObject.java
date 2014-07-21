package de.waldmensch;
import java.util.Date;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class SuPoxyDataObject {

	public SuPoxyDataObject(String JSONString) throws ParseException {

		JSONRaw = JSONString;
		JSONObject json;
		//SuPoxyUtils.log(JSONString);
		json = (JSONObject) new JSONParser().parse(JSONString);

		if(json.get("Timestamp") != null)
			Timestamp = SuPoxyUtils.LongToDate(Long.parseLong(json.get("Timestamp").toString().substring(6, 19)));
		else
			Timestamp = new Date();

		PV = Integer.parseInt( SuPoxyUtils.chkStringForNull(json.get("PV")));
		FeedIn = Integer.parseInt( SuPoxyUtils.chkStringForNull(json.get("FeedIn")));
		GridConsumption = Integer.parseInt( SuPoxyUtils.chkStringForNull(json.get("GridConsumption")));
		DirectConsumption = Integer.parseInt( SuPoxyUtils.chkStringForNull(json.get("DirectConsumption")));
		SelfConsumption = Integer.parseInt( SuPoxyUtils.chkStringForNull(json.get("SelfConsumption")));
		SelfSupply = Integer.parseInt( SuPoxyUtils.chkStringForNull(json.get("SelfSupply")));
		TotalConsumption = Integer.parseInt( SuPoxyUtils.chkStringForNull(json.get("TotalConsumption")));
		DirectConsumptionQuote = Integer.parseInt( SuPoxyUtils.chkStringForNull(json.get("DirectConsumptionQuote")));
		SelfConsumptionQuote = Integer.parseInt( SuPoxyUtils.chkStringForNull(json.get("SelfConsumptionQuote")));
		AutarkyQuote = Integer.parseInt( SuPoxyUtils.chkStringForNull(json.get("AutarkyQuote")));
		BatteryIn = Integer.parseInt( SuPoxyUtils.chkStringForNull(json.get("BatteryIn")));
		BatteryOut = Integer.parseInt( SuPoxyUtils.chkStringForNull(json.get("BatteryOut")));
		BatteryChargeStatus = Integer.parseInt( SuPoxyUtils.chkStringForNull(json.get("BatteryChargeStatus")));
		OperationHealth = Integer.parseInt( SuPoxyUtils.chkStringForNull(json.get("OperationHealth")));
		BatteryStateOfHealth = Integer.parseInt( SuPoxyUtils.chkStringForNull(json.get("BatteryStateOfHealth")));
		errors = (JSONArray)json.get("ErrorMessages");

		if (errors.size() > 0){
			ErrorMessages = new String[errors.size()];
			for (int i = 0; i < errors.size(); i++){
				ErrorMessages[i] = errors.get(i).toString();
				SuPoxyUtils.log("Got Errormessage: " + ErrorMessages[i]);
			}
		}

	}

	private Date Timestamp;
	private Integer PV;
	private Integer FeedIn;
	private Integer GridConsumption;
	private Integer DirectConsumption;
	private Integer SelfConsumption;
	private Integer SelfSupply;
	private Integer TotalConsumption;
	private Integer DirectConsumptionQuote;
	private Integer SelfConsumptionQuote;
	private Integer AutarkyQuote;
	private Integer BatteryIn;
	private Integer BatteryOut;
	private Integer BatteryChargeStatus;
	private Integer OperationHealth;
	private Integer BatteryStateOfHealth;
	private String[] ErrorMessages = new String[0];
	private JSONArray errors;
	private String JSONRaw;

	public String getJSONRaw() {
		return JSONRaw;
	}
	public void setJSONRaw(String jSONRaw) {
		JSONRaw = jSONRaw;
	}
	
	public String[] getErrorMessages() {
		return ErrorMessages;
	}
	public void setErrorMessages(String[] errorMessages) {
		ErrorMessages = errorMessages;
	}
	public Date getTimestamp() {
		return Timestamp;
	}
	public void setTimestamp(Date timestamp) {
		Timestamp = timestamp;
	}
	public Integer getPV() {
		return PV;
	}
	public void setPV(Integer pV) {
		PV = pV;
	}
	public Integer getFeedIn() {
		return FeedIn;
	}
	public void setFeedIn(Integer feedIn) {
		FeedIn = feedIn;
	}
	public Integer getGridConsumption() {
		return GridConsumption;
	}
	public void setGridConsumption(Integer gridConsumption) {
		GridConsumption = gridConsumption;
	}
	public Integer getDirectConsumption() {
		return DirectConsumption;
	}
	public void setDirectConsumption(Integer directConsumption) {
		DirectConsumption = directConsumption;
	}
	public Integer getSelfConsumption() {
		return SelfConsumption;
	}
	public void setSelfConsumption(Integer selfConsumption) {
		SelfConsumption = selfConsumption;
	}
	public Integer getSelfSupply() {
		return SelfSupply;
	}
	public void setSelfSupply(Integer selfSupply) {
		SelfSupply = selfSupply;
	}
	public Integer getTotalConsumption() {
		return TotalConsumption;
	}
	public void setTotalConsumption(Integer totalConsumption) {
		TotalConsumption = totalConsumption;
	}
	public Integer getDirectConsumptionQuote() {
		return DirectConsumptionQuote;
	}
	public void setDirectConsumptionQuote(Integer directConsumptionQuote) {
		DirectConsumptionQuote = directConsumptionQuote;
	}
	public Integer getSelfConsumptionQuote() {
		return SelfConsumptionQuote;
	}
	public void setSelfConsumptionQuote(Integer selfConsumptionQuote) {
		SelfConsumptionQuote = selfConsumptionQuote;
	}
	public Integer getAutarkyQuote() {
		return AutarkyQuote;
	}
	public void setAutarkyQuote(Integer autarkyQuote) {
		AutarkyQuote = autarkyQuote;
	}
	public Integer getBatteryIn() {
		return BatteryIn;
	}
	public void setBatteryIn(Integer batteryIn) {
		BatteryIn = batteryIn;
	}
	public Integer getBatteryOut() {
		return BatteryOut;
	}
	public void setBatteryOut(Integer batteryOut) {
		BatteryOut = batteryOut;
	}
	public Integer getBatteryChargeStatus() {
		return BatteryChargeStatus;
	}
	public void setBatteryChargeStatus(Integer batteryChargeStatus) {
		BatteryChargeStatus = batteryChargeStatus;
	}
	public Integer getOperationHealth() {
		return OperationHealth;
	}
	public void setOperationHealth(Integer operationHealth) {
		OperationHealth = operationHealth;
	}
	public Integer getBatteryStateOfHealth() {
		return BatteryStateOfHealth;
	}
	public void setBatteryStateOfHealth(Integer batteryStateOfHealth) {
		BatteryStateOfHealth = batteryStateOfHealth;
	}

}
