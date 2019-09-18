package com.krishagni.openspecimen.plugin.token;

import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;

import com.krishagni.catissueplus.core.biospecimen.domain.Specimen;
import com.krishagni.catissueplus.core.common.domain.AbstractLabelTmplToken;
import com.krishagni.catissueplus.core.common.domain.LabelTmplToken;
import com.krishagni.catissueplus.core.de.events.FormRecordSummary;
import com.krishagni.catissueplus.core.de.repository.FormDao;

import edu.common.dynamicextensions.domain.nui.Container;
import edu.common.dynamicextensions.domain.nui.Control;
import edu.common.dynamicextensions.napi.ControlValue;
import edu.common.dynamicextensions.napi.FormData;
import edu.common.dynamicextensions.napi.FormDataManager;
import edu.common.dynamicextensions.napi.impl.FormDataManagerImpl;

public class SpecimenBacteriaLISGermCodePrintToken extends AbstractLabelTmplToken implements LabelTmplToken {
	 
	private FormDao formDao;

	//
	// setter to inject formDao bean defined in core app context
	//
	public void setFormDao(FormDao formDao) {
		this.formDao = formDao;
	}	 	

	public SpecimenBacteriaLISGermCodePrintToken() {

	}

	@Override
    public String getName() {
        return "specimen_bacteria_lis_germ_code";
    }
 
    @Override
    public String getReplacement(Object object) {
        Specimen spmn = (Specimen) object;
        
        //
        // Step 1: Retrieve the form by name (SpecimenCustomDetails)
        //
        Container form = Container.getContainer("bacteriaFormUSB_1");
        if (form == null) {
           //
           // Return an empty string when there is no form by name "SpecimenCustomDetails"
           //
           return StringUtils.EMPTY;
        }

        //
        // Step 2: Retrieve all "SpecimenCustomDetails" records metadata for the input specimen
        //      
        Map<Long, List<FormRecordSummary>> records = formDao.getFormRecords(spmn.getId(), "SpecimenExtension", form.getId());
        if (records.isEmpty()) {
           //
           // Return an empty string when no records are saved for the specimen
           //
           return StringUtils.EMPTY;
        }
   
        //
        // Step 3: Sort form records based on their order of creation and
        // get the first record ID (records metadata)
        //
        FormRecordSummary record = records.get(form.getId()).stream()
           .sorted((r1, r2) -> r1.getRecordId().compareTo(r2.getRecordId()))
           .findFirst().get();
         
        //
        // Step 4: Get the form record data
        //
        FormDataManager mgr = new FormDataManagerImpl(false);
        FormData formData = mgr.getFormData(form, record.getRecordId());
   
        //
        // Step 5: Retrieve the field value that we are interested in
        // i.e. destructionDate
        //
        ControlValue value = formData.getFieldValue("DD2");

        return value.toString();
    }
}
