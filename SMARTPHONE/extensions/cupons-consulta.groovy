import com.movile.sdk.services.promotionhandler.PromotionHandlerClient;
import com.movile.sdk.services.promotionhandler.model.GetCouponsRequest;
import com.movile.sdk.services.promotionhandler.model.GetCouponsResponse;
import com.movile.sdk.services.promotionhandler.model.Coupon;

import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.util.Locale;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import com.movile.albatross.core.logging.Loggers;

import com.movile.sdk.services.atlasws.AtlasWSClient;
import com.movile.sdk.services.atlasws.model.PhoneHelperParseRequest;
import com.movile.sdk.services.atlasws.model.PhoneHelperParseResponse;
import com.movile.sdk.services.atlasws.model.enums.Carrier;
import com.movile.sdk.services.atlasws.model.enums.CountryCode;
import com.movile.sdk.services.atlasws.model.enums.PhoneHelperResultCode;

errorNoMsisdnMessage = "Por favor, informe o n&#250;mero do telefone celular!";
errorInvalidMsisdnMessage = "N&#250;mero inv&#225;lido";
errorNoCoupons = "Voc&#234; ainda n&#227;o possui cupons";

def prizeAppId = 74;

def ddd = request.getParameter("ddd");
def phone = request.getParameter("phone");

def manager = new CouponManager(applicationContext, modelMap)

def msisdn = null;
if (ddd && phone) {
     msisdn = manager.validateMsisdn(ddd + phone)
}

if (msisdn) {
    manager.getCoupons(msisdn.toLong(), prizeAppId.toLong());
} else {
    targetView = "meus_cupons";
}

/* As per a change in requirements, pin verification has been disabled. The code below is kept in case it's needed in the future
 *
 *
def msisdn = sessionMap.get("ext_MSISDN");
def passwordSent = sessionMap.get("ext_PINCODE");
def informedPwd = request.getParameter("passwd");
if (passwordSent != null && informedPwd != null) {
    if (!passwordSent.equals(informedPwd)) {
        //passord is not valid
        modelMap.put("resultStatus", "err");
        modelMap.put("resultMessage","Senha invalida!");
    } else {
        //valid password
        modelMap.put("resultStatus", "ok");
        modelMap.put("resultMessage","");
        sessionMap.remove("ext_PINCODE");
        sessionMap.remove("ext_MSISDN");
        getCoupons(msisdn.toLong(), prizeAppId.toLong())
    }
} else {
    //need to redirect user
    targetView = "meus_cupons"
    modelMap.put("resultStatus", "err");
    modelMap.put("resultMessage","Por favor, autentique-se <a href=meus_cupons.html>aqui</a>");
}
*/


class CouponManager {

  static private applicationContext
  static private modelMap

  Logger logger = Loggers.EXTENSION.getLogger();

  CouponManager(applicationContext, modelMap) {
    this.applicationContext = applicationContext
    this.modelMap = modelMap
  }

  def getCoupons(msisdn, prizeAppId) {
    logger.info("msisdn {}", msisdn);
      PromotionHandlerClient promotionHandlerClient = applicationContext.getBean(PromotionHandlerClient.class)

      GetCouponsRequest couponsRequest = new GetCouponsRequest()
      couponsRequest.setMsisdn(msisdn)
      couponsRequest.setPrizeAppId(prizeAppId)

      GetCouponsResponse couponsResponse = promotionHandlerClient.getCoupons(couponsRequest)

      List<Coupon> couponsList = couponsResponse.getCoupons()

      def coupons = []

      couponsList.each {
          coupons << parseCoupon(it)
      }

      modelMap.put("cupons", coupons)

      if (coupons.size() == 0) {
          modelMap.put("resultMessage", "Voc&#234; ainda n&#227;o possui cupons")
      }
  }

  def parseCoupon(coupon) {
    def len = coupon.number.toString().length();
    def couponNumber = "00000000".substring(len) + coupon.number.toString();
      return [
          parseDate(coupon.insertedTime.toString()),
          couponNumber,
          parseDate(coupon.drawDate.toString()),
  	      coupon.prize
      ]
  }

  def parseDate(date) {
      DateFormat df = new SimpleDateFormat("EEE MMM dd kk:mm:ss z yyyy", Locale.ENGLISH)
      Date newDate = df.parse(date)
      return newDate.format("dd/MM/yyyy")
  }

  def validateMsisdn(String msisdn) {
    if (StringUtils.isEmpty(msisdn)) {
        modelMap.put("resultStatus", "err");
        modelMap.put("resultMessage", errorNoMsisdnMessage);
        return null
    }
    if (!(msisdn.length() in [10,11] && msisdn ==~ /[0-9]*/)) {
        modelMap.put("resultStatus", "err");
        modelMap.put("resultMessage", errorInvalidMsisdnMessage);
        return null
    }
    return '55' + msisdn
}

}
