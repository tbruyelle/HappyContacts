/**
 * 
 */
package com.tsoft.happycontacts;

import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceClickListener;

/**
 * @author tom
 * 
 */
public class HappyContactsPreferences
    extends PreferenceActivity
{
  private OnPreferenceClickListener mAlarmToggleClickListener = new OnPreferenceClickListener()
  {
    @Override
    public boolean onPreferenceClick(Preference preference)
    {
      if (AlarmSetterService.isAlarmUp(HappyContactsPreferences.this))
      {
        AlarmSetterService.cancelAlarm(HappyContactsPreferences.this);
        preference.setSummary(R.string.pref_state_off);
      }
      else
      {        
        AlarmSetterService.startAlarm(HappyContactsPreferences.this);
        preference.setSummary(R.string.pref_state_on);
      }
      return true;
    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setPreferenceScreen(createPreferenceHierarchy());
  }

  private PreferenceScreen createPreferenceHierarchy()
  {
    // Root
    PreferenceScreen root = getPreferenceManager().createPreferenceScreen(this);

    // State
    CheckBoxPreference statePref = new CheckBoxPreference(this);
    statePref.setTitle(R.string.pref_state);
    if (AlarmSetterService.isAlarmUp(this))
    {
      statePref.setSummary(R.string.pref_state_on);
      statePref.setChecked(true);
    }
    else
    {
      statePref.setSummary(R.string.pref_state_off);
      statePref.setChecked(false);
    }
    statePref.setOnPreferenceClickListener(mAlarmToggleClickListener);
    root.addPreference(statePref);

    // test app
    PreferenceScreen testAppPref = getPreferenceManager().createPreferenceScreen(this);
    testAppPref.setKey("testAppPref");
    testAppPref.setTitle(R.string.pref_test_app);
    testAppPref.setSummary(R.string.pref_test_app_summary);
    testAppPref.setIntent(new Intent(this, TestAppActivity.class));
    root.addPreference(testAppPref);

    // blacklist
    PreferenceScreen blackListPref = getPreferenceManager().createPreferenceScreen(this);
    blackListPref.setKey("blackListPref");
    blackListPref.setTitle(R.string.pref_blacklist);
    blackListPref.setSummary(R.string.pref_blacklist_summary);
    blackListPref.setIntent(new Intent(this, BlackListActivity.class));
    root.addPreference(blackListPref);

    // templates
    PreferenceCategory templatesPrefCat = new PreferenceCategory(this);
    templatesPrefCat.setTitle(R.string.pref_templates_cat);
    root.addPreference(templatesPrefCat);

    EditTextPreference templateSmsPref = new EditTextPreference(this);
    templateSmsPref.setDialogTitle(R.string.pref_template_sms_dialog);
    templateSmsPref.setKey("templateSmsPref");
    templateSmsPref.setTitle(R.string.pref_template_sms);
    templateSmsPref.setSummary(R.string.pref_template_sms_summary);
    templatesPrefCat.addPreference(templateSmsPref);

    EditTextPreference templateEmailPref = new EditTextPreference(this);
    templateEmailPref.setDialogTitle(R.string.pref_template_email_dialog);
    templateEmailPref.setKey("templateEmailPref");
    templateEmailPref.setTitle(R.string.pref_template_email);
    templateEmailPref.setSummary(R.string.pref_template_email_summary);
    templatesPrefCat.addPreference(templateEmailPref);

    return root;
  }
}
