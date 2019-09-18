package com.krishagni.openspecimen.plugin.init;

import org.springframework.beans.factory.InitializingBean;
import com.krishagni.catissueplus.core.common.domain.LabelTmplTokenRegistrar;
import com.krishagni.openspecimen.plugin.token.SpecimenBacteriaLISDayNumberPrintToken;
import com.krishagni.openspecimen.plugin.token.SpecimenBacteriaLISGermCodePrintToken;
 
 
public class PluginInitializer implements InitializingBean {
   
  private LabelTmplTokenRegistrar specimenPrintLabelTokensRegistrar;
  private SpecimenBacteriaLISGermCodePrintToken lisGermCodePrintToken;
  private SpecimenBacteriaLISDayNumberPrintToken lisDayNumberPrintToken;
  
  public void setSpecimenPrintLabelTokensRegistrar(LabelTmplTokenRegistrar specimenPrintLabelTokensRegistrar) {
      this.specimenPrintLabelTokensRegistrar = specimenPrintLabelTokensRegistrar;
  }

  public void setLisGermCodePrintToken(SpecimenBacteriaLISGermCodePrintToken lisGermCodePrintToken) {
      this.lisGermCodePrintToken = lisGermCodePrintToken;
  }

  public void setLisDayNumberPrintToken(SpecimenBacteriaLISDayNumberPrintToken lisDayNumberPrintToken) {
      this.lisDayNumberPrintToken = lisDayNumberPrintToken;
  }
  
  @Override
  public void afterPropertiesSet() throws Exception {
      specimenPrintLabelTokensRegistrar.register(lisGermCodePrintToken);
      specimenPrintLabelTokensRegistrar.register(lisDayNumberPrintToken);
  }
}