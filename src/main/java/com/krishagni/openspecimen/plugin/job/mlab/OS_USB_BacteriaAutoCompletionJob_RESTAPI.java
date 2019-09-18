package com.krishagni.openspecimen.plugin.job.mlab;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

public class OS_USB_BacteriaAutoCompletionJob_RESTAPI {

	private static RestTemplate template = new RestTemplate();
	
	private static final String API_BASE_URL = "http://dev.lab.biobank.usb.ch/rest/ng";
	
	
	public void doBacteriaSpecimensAutoCompletion() {	
				
			String token = login();
			
	        try {
	        	
	        	// get CPs for Clinical Microbiology (CMB)
				Map<String, Object>[] cps = getCPSMetadata(token, "Clinical Microbiology");
				// get NCBITax Dropdown -> JSON Array
				String ncbiTaxString = getNCBITaxDropDown(token);
				JSONArray ncbiTaxJson = new JSONArray(ncbiTaxString);
				// iterate CPs for CMB
				for (Map cp : cps) {
					//System.out.println(cp.get("shortTitle"));
					//if (cp.get("shortTitle").toString().equalsIgnoreCase("Allgemein Test CP")) {
						String cpShortTitle = cp.get("shortTitle").toString();
						int cpId = (int) cp.get("id");
						// get specimen ids in CP
						Map<String, Object>[] spmns = getSpecimensForCp(token, cpId);
						
						for (Map spcm : spmns) {
							long createdOn = Long.parseLong(spcm.get("createdOn").toString());		
							long spcmId = Long.parseLong(spcm.get("id").toString());	
							String spcmLabel = spcm.get("label").toString();
							long current = System.currentTimeMillis();
			            	long oneDay = 1000 * 60 * 60 * 24;
			            	boolean spmcLast24Hours = false;
			            	spmcLast24Hours= current-createdOn < oneDay;	            	
			            	// continue if specimen was created in last 24 hours and update NCBI details
			            	if(spmcLast24Hours) {
			            		
			            		Map<String, Object> spcmDetails = getSpecimenDetails(token, spcmId);		           		
			            		Map<String, Object> extension = (Map<String, Object>) spcmDetails.get("extensionDetail");		            		
			            		int id = (int) extension.get("id");
			            		int objectId = (int) extension.get("objectId");
			            		int formId = (int) extension.get("formId");
			            		String formCaption = (String) extension.get("formCaption");		            		
			            		List<Map<String, Object>> extensionAttr = (List<Map<String, Object>>) extension.get("attrs");		            		
			            		String mLabGermCode = (String) extensionAttr.get(1).get("value");
			            		String mLabNumber = (String) extensionAttr.get(8).get("value");	  	
			            		long ncbiTaxDDID = 0;
			            		for(int z = 0; z < ncbiTaxJson.length(); z++) {
			            			if(ncbiTaxJson.getJSONObject(z).getString("value").equalsIgnoreCase(mLabGermCode)) {
			            				ncbiTaxDDID = ncbiTaxJson.getJSONObject(z).getLong("id");
			            			}		            			
			            		}
			            		
			            		if(ncbiTaxDDID != 0) {
			            					            			
			            			String ncbiDDJsonString = getNCBITaxDropDownByID(token, ncbiTaxDDID);
			            			JSONObject ncbiTaxInfo = new JSONObject(ncbiDDJsonString);
			            			JSONObject ncbiTaxInfoProps = ncbiTaxInfo.getJSONObject("props");
			            			  			
			            			int dynamicSize = 3;
				            		int year = Calendar.getInstance().get(Calendar.YEAR);	
				            		
				            		HashMap<String, Object> specimen = new HashMap<>();
				            		
				            		HashMap<String, Object> extensionDetails = new HashMap<>();
				            		extensionDetails.put("id", id);
				            		extensionDetails.put("objectId", objectId);
				            		extensionDetails.put("formId", formId);
				            		extensionDetails.put("formCaption", formCaption);
				            		
				            		if (mLabNumber.startsWith(year+"-"))
				            			dynamicSize--;
				            			
				            		
				            		Map[] attrs = new Map[dynamicSize];
				            		
				            		HashMap<String, Object> dd16 = new HashMap<>();		
				            		dd16.put("name", "DD16");
				            		dd16.put("udn", "sbp_species_name_14");
				            		dd16.put("caption", "Species Name");
				            		dd16.put("value", ncbiTaxInfoProps.get("Species Name"));
				            		attrs[0] = dd16;
				            		
				            		HashMap<String, Object> st17 = new HashMap<>();		
				            		st17.put("name", "ST17");
				            		st17.put("udn", "usb_taxonomy_id_15");
				            		st17.put("caption", "Taxonomy Name");
				            		st17.put("value", ncbiTaxInfoProps.get("Taxonomy ID"));
				            		attrs[1] = st17;
				            	
				            		
				            		
				            		if (!mLabNumber.startsWith(year+"-")) {			            		
					            		HashMap<String, Object> st8 = new HashMap<>();		
					            		st8.put("name", "ST8");
					            		st8.put("udn", "usb_lis_day_number_3");
					            		st8.put("caption", "Day Name");
					            		st8.put("value", year+"-"+mLabNumber);
					            		attrs[2] = st8;		            			
				            		}
				            		
				            		String[] biohazards = new String[1];
				            		
				            		if(!ncbiTaxInfoProps.isNull("Biosafety") || !ncbiTaxInfoProps.get("Biosafety").toString().isEmpty())
				            			biohazards[0] = ncbiTaxInfoProps.get("Biosafety").toString();
		     		
				            		extensionDetails.put("attrs", attrs);
				            		specimen.put("extensionDetail", extensionDetails);
				            		if(!ncbiTaxInfoProps.isNull("Biosafety") || !ncbiTaxInfoProps.get("Biosafety").toString().isEmpty())
				            			specimen.put("biohazards", biohazards);
				            		specimen.put("id", spcmId);
				            		
				            		System.out.println("CP: "+cpShortTitle+" -> Updating Specimen with label "+spcmLabel+" (LIS Germ Code: "+mLabGermCode+", LIS Day Number: "+mLabNumber+") -> (Species Name: "+ncbiTaxInfoProps.get("Species Name")+", "
						            		+", NCBI Taxonomy ID: "+ncbiTaxInfoProps.get("Taxonomy ID")+", LIS Day Number: "+year+"-"+mLabNumber+", Biosafety: "+ncbiTaxInfoProps.get("Biosafety").toString()+")");
						            		
				            		
				            		updateSpecimenDetails(token, spcmId, specimen);	
			            				            			
			            		} else {
			            			System.out.println("CP: "+cpShortTitle+" -> Updating Specimen with label "+spcmLabel+" (LIS Germ Code: "+mLabGermCode+", LIS Day Number: "+mLabNumber+") -> (No Mapping for LIS Germ Code to NCBI Taxonomy)");
			            		}	
				            
			            	}
						}
					//}				
				}			
		
			} catch (Exception e) {
				logout(token);
				e.printStackTrace();		
			}
       
        logout(token);
	}

	private static String login() {
	      //
	      // username and password could be fetched from your client configuration database
	      //
	      ObjectMapper objectMapper = new ObjectMapper();
	      MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();
	      messageConverter.setPrettyPrint(false);
	      messageConverter.setObjectMapper(objectMapper);
	      messageConverter.setSupportedMediaTypes(Arrays.asList(new MediaType("application", "json", MappingJackson2HttpMessageConverter.DEFAULT_CHARSET), new MediaType("text", "javascript", MappingJackson2HttpMessageConverter.DEFAULT_CHARSET)));
	      template.getMessageConverters().removeIf(m -> m.getClass().getName().equals(MappingJackson2HttpMessageConverter.class.getName()));
	      template.getMessageConverters().add(messageConverter);
	   	  template.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
		
	      Map<String, Object> payload = new HashMap<>();
	      payload.put("loginName", "internalapi");
	      payload.put("password", "Xa65&lVz9?oS");
	      Map<String, Object>  resp = template.postForObject(getUrl("/sessions"), payload, Map.class);
	      return (String) resp.get("token");
	   }	
 
	 private static <T> T invokeApi(HttpMethod httpMethod, String uri, String token, Object body, Class<T> returnType) {
		      ResponseEntity<T> resp = template.exchange(getUrl(uri), httpMethod, getHttpEntity(token, body), returnType);
		      if (resp.getStatusCode() == HttpStatus.OK) {
		         return resp.getBody();
		      }
		 
		      //
		      // Handle API failures
		      //
		      throw new RuntimeException("Error invoking API: " + uri);
		   }
		 
	 private static HttpEntity<?> getHttpEntity(String token) {
		      return getHttpEntity(token, null);
		   }
		 
	 private static HttpEntity<?> getHttpEntity(String token, Object body) {
	    HttpHeaders headers = new HttpHeaders();
	    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
	    headers.set("X-OS-API-TOKEN", token); // API auth token header
	    return new HttpEntity<>(body, headers);
	 }
	 
	 private static String getUrl(String uri) {
	    return API_BASE_URL + uri;
	 }
	 
	 private Map<String, Object>[] getCPSMetadata(String token, String department) throws Exception {
	      String uri = null;
	      if (department.equalsIgnoreCase("Clinical Microbiology")) {
	          uri = "/collection-protocols?repositoryName="+department;
	       }      
	      Map[] cps = invokeApi(HttpMethod.GET, uri, token, null, Map[].class); 
	      return cps;	 
	 }
	 
	 private Map<String, Object>[] getSpecimensForCp(String token, int cpId) throws Exception {
	      String uri = "/specimens?cpId="+cpId;  
	      Map[] spmns = invokeApi(HttpMethod.GET, uri, token, null, Map[].class); 
	      return spmns;	 
	 }
	 
	 private Map<String, Object> getFormMetadata(String token, String entityType, Long objectId, String formName) throws Exception {
		      String uri = null;
		      if (entityType.equals("Specimen")) {
		         uri = "/specimens/" + objectId + "/forms";
		      } else if (entityType.equals("Visit")) {
		         uri = "/visits/" + objectId + "/forms";
		      } else if (entityType.equals("CollectionProtocolRegistration")) {
		         uri = "/collection-protocol-registrations/" + objectId + "/forms";
		      } else {
		         throw new IllegalArgumentException("Invalid entity type: " + entityType);
		      }
		 
		      Map[] forms = invokeApi(HttpMethod.GET, uri, token, null, Map[].class);
		      for (Map form : forms) {
		         if (Objects.equals(form.get("formCaption"), formName)) {
		            return form;
		         }
		      }
		 
		      return null;
		   }
	 
	 
	private Map<String, Object> getSpecimenDetails(String token, long spcmId) {
		String uri = "/specimens/"+spcmId;  
	    Map spmn = invokeApi(HttpMethod.GET, uri, token, null, Map.class); 
	    return spmn;	
	} 
	
	
	private long updateSpecimenDetails(String token, long spcmId, Map<String, Object> spcmDetails) {
		String uri = "/specimens/"+spcmId; 
		Map savedSpmn = invokeApi(HttpMethod.PUT, uri, token, spcmDetails, Map.class); 
		return ((Number)savedSpmn.get("id")).longValue();
	}
	
	
	 private static Long saveFormData(String token, Long specimenId, Map<String, Object> formMetadata) {
	      Map<String, Object> appData = new HashMap<>();
	      //appData.put("useUdn",     true); // to let API know we use human readable field names instead of cryptic ones
	      //appData.put("formCtxtId", formMetadata.get("formCtxtId"));
	      appData.put("objectId",   specimenId);

	      Map<String, Object> formData = new HashMap<>();
	      formData.put("appData", appData);
	 
	      //
	      // form field values
	      //
	      //formData.put("destructionDate", destructionDate);
	      formData.put("destructionMethod", "Cremation");
	 
	      Number formId = (Number)formMetadata.get("formId");
	      String uri = "/forms/" + formId + "/data";
	      Map savedData = invokeApi(HttpMethod.POST, uri, token, formData, Map.class);
	 
	      //
	      // return record ID on saving form data
	      //
	      return ((Number)savedData.get("id")).longValue();
	   }
		
	 private String getNCBITaxDropDown(String token) {
			String uri = "/permissible-values/?attribute=NCBITax";  
			String ncbiTax = invokeApi(HttpMethod.GET, uri, token, null, String.class); 
		    return ncbiTax;	
		} 
	 
	 private String getNCBITaxDropDownByID(String token, long ddid) {
			String uri = "/permissible-values/"+ddid;  
			String ncbiTax = invokeApi(HttpMethod.GET, uri, token, null, String.class); 
		    return ncbiTax;	
		} 
	 
	private static void logout(String token) {
	      invokeApi(HttpMethod.DELETE, "/sessions", token, null, Map.class);
	      //template.exchange(getUrl("/sessions"), HttpMethod.DELETE, getHttpEntity(token), Map.class);
	   }
}
