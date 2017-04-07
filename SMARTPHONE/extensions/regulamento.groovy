import com.movile.sdk.services.odin.OdinClient

def rulesOdinKey = "timgourmet.rules"
    rules = new Rules(rulesOdinKey,  applicationContext, modelMap)
    rules.getRules()

class Rules {

    static private rulesOdinKey
    static private applicationContext
    static private modelMap


    Rules (rulesOdinKey, applicationContext, modelMap) {
        this.rulesOdinKey = rulesOdinKey
        this.applicationContext = applicationContext
        this.modelMap = modelMap
    }

    public def getRules() {
        OdinClient odinClient = applicationContext.getBean(OdinClient.class)
        modelMap.put("rules", odinClient.getFormattedText(this.rulesOdinKey))
    }


}
