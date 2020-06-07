package jonnelafin.lemmyMobile;

import static com.codename1.ui.CN.*;
import com.codename1.components.SpanLabel;
import com.codename1.io.JSONParser;
import com.codename1.io.websocket.WebSocket;
import com.codename1.ui.Button;
import com.codename1.ui.Dialog;
import com.codename1.ui.Form;
import com.codename1.ui.plaf.UIManager;
import com.codename1.ui.util.Resources;
import com.codename1.ui.Container;
import com.codename1.ui.FontImage;
import com.codename1.ui.PickerComponent;
import com.codename1.ui.TextComponent;
import com.codename1.ui.Toolbar;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.layouts.TextModeLayout;
import com.codename1.ui.validation.GroupConstraint;
import com.codename1.ui.validation.LengthConstraint;
import com.codename1.ui.validation.RegexConstraint;
import com.codename1.ui.validation.Validator;
import java.util.HashMap;
import java.util.Map;

/**
 * This file was generated by <a href="https://www.codenameone.com/">Codename One</a> for the purpose 
 * of building native mobile applications using Java.
 */
public class LemmyMain {

    private Form current;
    private Resources theme;

    private Form home;

    public void init(Object context) {
        // use two network threads instead of one
        updateNetworkThreadCount(2);

        theme = UIManager.initFirstTheme("/theme");

        // Enable Toolbar on all Forms by default
        Toolbar.setGlobalToolbar(true);

        // Pro only feature
        //Log.bindCrashProtection(true);

        addNetworkErrorListener(err -> {
            // prevent the event from propagating
            err.consume();
            if(err.getError() != null) {
                //Log.e(err.getError());
            }
            //Log.sendLogAsync();
            Dialog.show("Connection Error", "There was a networking error in the connection to " + err.getConnectionRequest().getUrl(), "OK", null);
        });        
    }

    public void start() {
        if (current != null) {
            current.show();
            return;
        }

        TextModeLayout tm = new TextModeLayout(4, 2);
        home = new Form("Home", new BorderLayout());
        Container content = new Container(tm);
        content.add(tm.createConstraint().horizontalSpan(2), new SpanLabel("Welcome to Lemmy!"));
        
        TextComponent name = new TextComponent().labelAndHint("Name");
        content.add(tm.createConstraint().horizontalSpan(2), name);

        TextComponent bio = new TextComponent().labelAndHint("Bio").multiline(true).rows(3);
        content.add(tm.createConstraint().horizontalSpan(2), bio);

        PickerComponent gender = PickerComponent.createStrings("Unspecified", "Male", "Female", "Other").label("Gender");
        content.add(gender);

        PickerComponent dateOfBirth = PickerComponent.createDate(null).label("Birthday");
        //content.add(dateOfBirth);
        
        content.setScrollableY(true);
                
        Button submit = new Button("Submit");
        FontImage.setMaterialIcon(submit, FontImage.MATERIAL_DONE);
        submit.addActionListener(e -> {
            showOKForm(name.getField().getText());
        });
        
        home.add(CENTER, content);
        home.add(SOUTH, submit);
        
        Validator val = new Validator();
        val.setShowErrorMessageForFocusedComponent(true);
        val.addConstraint(name, 
                new GroupConstraint(
                        new LengthConstraint(2), 
                        new RegexConstraint("^([a-zA-Z ]*)$", "Please only use latin characters for name"))).
                addSubmitButtons(submit);

        home.show();
        showOKForm("User");
    }

    private void showOKForm(String name) {
        Form f = new Form("Thanks", BoxLayout.y());
        SpanLabel loadInfo = new SpanLabel("Loading submissions...");
        f.add(loadInfo);
        f.getToolbar().setBackCommand("", e -> home.showBack());
        f.show();
        
        //Load contents
        String host = "forum.mesbees.com";
        String url = "wss://" + host + "/api/v1/ws";
        WebSocket sock = new WebSocket(url) {
            @Override
            protected void onOpen() {
                System.out.println("Websocket opened!");
                System.out.println("Making request...");
                Map<String,String> arg = new HashMap();
                arg.put("op", "ListCategories");
                System.out.println(JSONParser.mapToJson(arg));
                this.send(JSONParser.mapToJson(arg));
            }
            
            @Override
            protected void onClose(int arg0, String arg1) {
                System.out.println("Websocket closed!");
            }
            
            @Override
            protected void onMessage(String arg0) {
                System.out.println("Incoming data: ");
            }
            
            @Override
            protected void onMessage(byte[] arg0) {
                System.out.println("Incoming data: " + arg0);
            }

            @Override
            protected void onError(Exception arg0) {
                System.out.println("Error: " + arg0);
            }
        };
        sock.connect();
    }
    
    public void stop() {
        current = getCurrentForm();
        if(current instanceof Dialog) {
            ((Dialog)current).dispose();
            current = getCurrentForm();
        }
    }

    public void destroy() {
    }

}