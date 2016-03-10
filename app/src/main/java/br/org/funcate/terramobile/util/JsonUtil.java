package br.org.funcate.terramobile.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import br.org.funcate.dynamicforms.FormUtilities;
import br.org.funcate.dynamicforms.TagsManager;

import static br.org.funcate.dynamicforms.FormUtilities.TAG_KEY;

/**
 * Created by andre on 10/03/16.
 */
public class JsonUtil {

    public static ArrayList<String> getKeysForm(String jsonForm) {

        ArrayList<String> keys=new ArrayList<String>();

        if(jsonForm.isEmpty()) {
            System.out.println("Parameter JSON form is empty.");
            return keys;
        }

        try {

            JSONObject sectionObject;
            String defaultSectionName = FormUtilities.DEFAULT_SESSION_NAME;
            sectionObject = TagsManager.getInstance(jsonForm).getSectionByName(defaultSectionName);

            if(sectionObject==null) {
                System.out.println("Failure on parse JSON form.");
                return keys;
            }

            List<String> availableFormNames = TagsManager.getFormNames4Section(sectionObject);
            for (String formName : availableFormNames) {

                JSONObject formObject = TagsManager.getForm4Name(formName, sectionObject);

                JSONArray formItemsArray = TagsManager.getFormItems(formObject);

                int length = formItemsArray.length();
                for (int i = 0; i < length; i++) {
                    JSONObject jsonObject = formItemsArray.getJSONObject(i);

                    if (jsonObject.has(TAG_KEY))
                        keys.add(jsonObject.getString(TAG_KEY).trim());
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
            return keys;
        }


        return keys;
    }
}
