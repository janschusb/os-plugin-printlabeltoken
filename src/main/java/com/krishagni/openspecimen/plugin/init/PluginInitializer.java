package com.krishagni.openspecimen.plugin.init;

import org.springframework.beans.factory.InitializingBean;
import com.krishagni.catissueplus.core.common.domain.LabelTmplTokenRegistrar;
import com.krishagni.openspecimen.plugin.token.SpecimenBacteriaGermAbbPrintToken;
 
 
public class PluginInitializer implements InitializingBean {
   
  private LabelTmplTokenRegistrar specimenPrintLabelTokensRegistrar;
  private SpecimenBacteriaGermAbbPrintToken germAbbrPrintToken;
  
  public void setSpecimenPrintLabelTokensRegistrar(LabelTmplTokenRegistrar specimenPrintLabelTokensRegistrar) {
      this.specimenPrintLabelTokensRegistrar = specimenPrintLabelTokensRegistrar;
  }
  
  public void setGermAbbrPrintToken(SpecimenBacteriaGermAbbPrintToken germAbbrPrintToken) {
      this.germAbbrPrintToken = germAbbrPrintToken;
  }
 
  @Override
  public void afterPropertiesSet() throws Exception {
      specimenPrintLabelTokensRegistrar.register(germAbbrPrintToken);
  }
}