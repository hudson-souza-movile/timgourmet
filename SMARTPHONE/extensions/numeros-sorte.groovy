import com.movile.sdk.services.atlasws.model.enums.ResponseStatus;

import com.movile.sdk.services.atlasws.AtlasWSClient;
import com.movile.sdk.services.atlasws.model.SendMessageRequest;
import com.movile.sdk.services.atlasws.model.SendMessageResponse;
import com.movile.sdk.services.atlasws.model.enums.ResponseStatus;
import com.movile.sdk.services.atlasws.model.enums.MechanicsType;

import com.movile.sdk.services.atlasws.AtlasWSClient;
import com.movile.sdk.services.atlasws.model.PhoneHelperParseRequest;
import com.movile.sdk.services.atlasws.model.PhoneHelperParseResponse;
import com.movile.sdk.services.atlasws.model.enums.Carrier;
import com.movile.sdk.services.atlasws.model.enums.CountryCode;
import com.movile.sdk.services.atlasws.model.enums.PhoneHelperResultCode;




/*
 * Default values
 *
 * smsConfiguration is a map
 *
 * */
def smsConfigurationMap = ["applicationId":759, "shortCode":"3040", "carrierId":5, "referenceId":"", "systemIdentifier":"TIM Gourmet", "smsMessage":"Digite a senha #pin# no site "]
def smsMessageMap = ["smsWrongPwd":"Senha que foi digitada nao confere!", "smsErr":"Nao foi possivel completar sua solicitacao. Por favor, tente novamente!", "smsSuccess":"Voce recebera uma senha em seu celular. Digite no site", "errNoMsisdn":"Por favor, informe o numero do telefone celular!", "errNoStep":"Ops, ocorreu um erro! Por favor, tente novamente!"]

def errorNoStepMessage = "Ops, ocorreu um erro! Por favor, tente novamente!"
def errorNoMsisdnMessage = "Por favor, informe o numero do telefone celular!"

/* getRandomNumber
 * Generate a int random number
 *
 * @param num Range to generate random number
 * @return a String - The generated random number with zero pad length(4)
 *
 * */
def getRandomNumber(int num) {
    Random random = new Random()
    return String.format("%04d", random.nextInt((10 * num)))
}

/* sendSMS
 *
 * Send a SMS message using Atlas Web Services (in movile sdk)
 *
 * @msisdn long cellphone
 * @smsConfig map with defaults values to send MT
 *
 * return boolean with send sms result
 * */
def sendSMS(long msisdn, smsConfig) {
    AtlasWSClient atlasWSClient = applicationContext.getBean("atlasWSClientSync");

    SendMessageRequest sendMessageRequest = new SendMessageRequest();
    sendMessageRequest.setApplicationId(smsConfig.applicationId);
    sendMessageRequest.setReferenceId(smsConfig.referenceId);
    sendMessageRequest.setMechanics(MechanicsType.INTERACTIVITY);
    sendMessageRequest.setSource(smsConfig.shortCode);
    sendMessageRequest.setCarrierId(smsConfig.carrierId);
    sendMessageRequest.setDestination(msisdn);

    String pinCode = getRandomNumber(1000)

    sessionMap.put("ext_PINCODE", pinCode);

    sendMessageRequest.setMessageText(smsConfig.smsMessage.replace("#pin#",pinCode));
    sendMessageRequest.setSystemIdentifier(smsConfig.systemIdentifier);

    SendMessageResponse sendMessageResponse = atlasWSClient.sendMessage(sendMessageRequest, null);

    return (sendMessageResponse != null && ResponseStatus.OK.equals(sendMessageResponse.getStatus()));
}

def validateMsisdn(String msisdn) {
  AtlasWSClient atlasWSClient = applicationContext.getBean("atlasWSClientSync");
  PhoneHelperParseRequest parseRequest = new PhoneHelperParseRequest(phoneNumber);
    parseRequest.setCountryCode(CountryCode.BR);

      PhoneHelperParseResponse parseResponse = atlasWSClient.parse(parseRequest);

      if (parseResponse == null || parseResponse.getResultCode() != PhoneHelperResultCode.VALID_NUMBER) {
          return null
      }

    return parse.getPhoneNumber()
}

def ddd = request.getParameter("ddd");
def phone = request.getParameter("phone");

def msisdn = null;
if (ddd != null && phone != null) {
    msisdn = validateMsisdn(ddd + phone);
}

//clear flag control
modelMap.remove("passwordSent");

if (msisdn != null) {

    sessionMap.put("ext_MSISDN", msisdn);
    modelMap.put("resultStatus", "ok");

    if (sendSMS(msisdn.toLong(), smsConfigurationMap)) {
        //MT was sent
        modelMap.put("resultStatus", "ok");
        modelMap.put("resultMessage", smsMessageMap.smsSuccess);
        modelMap.put("passwordSent","true");
    } else {
        modelMap.put("resultStatus", "err");
        modelMap.put("resultMessage", smsMessageMap.smsErr);
    }
}
