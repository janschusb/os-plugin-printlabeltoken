package com.krishagni.openspecimen.plugin.job;

import org.springframework.beans.factory.annotation.Configurable;

import com.krishagni.catissueplus.core.administrative.domain.ScheduledJobRun;
import com.krishagni.catissueplus.core.administrative.services.ScheduledTask;
import com.krishagni.catissueplus.core.common.PlusTransactional;

@Configurable
public class USBBacteriaSpecimenJob implements ScheduledTask {
	
	@Override
	@PlusTransactional
	public void doJob(ScheduledJobRun arg0) throws Exception {
		System.out.println("Executing USBBacteriaSpecimenJob started");
		OS_USB_BacteriaAutoCompletionJob_RESTAPI USBBacteriaJob = new OS_USB_BacteriaAutoCompletionJob_RESTAPI();
		USBBacteriaJob.doBacteriaSpecimensAutoCompletion();
		System.out.println("Executing USBBacteriaSpecimenJob ended with success");
	}
	
	

}
