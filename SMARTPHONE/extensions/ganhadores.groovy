import java.text.DateFormat
import java.text.SimpleDateFormat

import com.movile.sdk.services.promotionhandler.PromotionHandlerClient
import com.movile.sdk.services.promotionhandler.model.GetWinnersRequest
import com.movile.sdk.services.promotionhandler.model.GetWinnersResponse

def prizeAppId = [74]
def applicationId = 759

winners = new Winners(prizeAppId, applicationId, applicationContext, modelMap, cache)
winners.getWinners()

class Winners {
    static private prizeAppId
    static private applicationId
    static private applicationContext
    static private modelMap
    static private cache

    private static DATE_PATTERN = "EEE MMM dd kk:mm:ss z yyyy"
    private static DATE_FORMAT = "dd/MM/yyyy"
    private static PARSED_MSISDN_PARENTHESIS_1 = "("
    private static PARSED_MSISDN_PARENTHESIS_2 = ") "
    private static PARSED_MSISDN_DASH = "-"

    Winners (prizeAppId, applicationId, applicationContext, modelMap, cache) {
        this.prizeAppId = prizeAppId
        this.applicationId = applicationId
        this.applicationContext = applicationContext
        this.modelMap = modelMap
        this.cache = cache;
    }

    public def getWinners() {
        def winners;
        if (cache.get("ganhadores") == null) {
            PromotionHandlerClient promotionHandlerClient = applicationContext.getBean(PromotionHandlerClient.class);

            def winnersByApplicationId = [];
            this.prizeAppId.each{ prize ->
                GetWinnersRequest winnersRequest = new GetWinnersRequest();
                winnersRequest.setPrizeAppId(prize);

                GetWinnersResponse winnersResponse = promotionHandlerClient.getWinners(winnersRequest);

                winnersResponse.getWinners().each{ winner ->
                    winnersByApplicationId.add(parseWinner(winner));
                }
            }

            winners = (winnersByApplicationId.sort { a, b ->
                DateFormat df = new SimpleDateFormat(DATE_FORMAT);
                Date dateA = df.parse(a[2]);
                Date dateB = df.parse(b[2]);
                return dateA.compareTo(dateB);
            }).reverse();

            cache.putAt("ganhadores", winners);
        } else {
            winners = cache.get("ganhadores");
            cache.remove("ganhadores");
        };

        modelMap.put("ganhadores", winners);
    }

    private def parseDate(date) {
        return DateFormatUtils.format(date, "dd/MM/yyyy")
    }

    private def parseMsisdn(msisdn) {
        def ddd = msisdn[2..3]
        def prefix = msisdn[4..-5]
        def suffix = msisdn[-4..-1]

        /* replace numbers in the prefix for '*' */
        prefix = (prefix =~ /\d/).replaceAll("*")
        return PARSED_MSISDN_PARENTHESIS_1 + ddd + PARSED_MSISDN_PARENTHESIS_2 + prefix + PARSED_MSISDN_DASH + suffix
    }

    private def parseWinner(winner) {
        return [
            parseMsisdn(winner.userId.toString()),
            String.format("%08d", winner.couponNumber),
            parseDate(winner.insertedTime),
            winner.prize
        ]
    }
}
