/**
 * 
 */
package com.tsoft.happycontacts;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceClickListener;
import android.widget.TimePicker;

import com.tsoft.utils.AndroidUtils;

/**
 * @author tom
 * 
 */
public class HappyContactsPreferences
    extends PreferenceActivity
{
  public static String appName = "com.tsoft.HappyContacts";

  private static final int TIME_DIALOG_ID = 0;

  private OnPreferenceClickListener mAlarmToggleClickListener = new OnPreferenceClickListener()
  {
    @Override
    public boolean onPreferenceClick(Preference preference)
    {
      if (AlarmController.isAlarmUp(HappyContactsPreferences.this))
      {
        AlarmController.cancelAlarm(HappyContactsPreferences.this);
        preference.setSummary(R.string.pref_state_off);
      }
      else
      {
        AlarmController.startAlarm(HappyContactsPreferences.this);
        preference.setSummary(R.string.pref_state_on);
      }
      return true;
    }
  };

  private TimePickerDialog.OnTimeSetListener mTimeSetListener = new TimePickerDialog.OnTimeSetListener()
  {
    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute)
    {
      mAlarmHour = hourOfDay;
      mAlarmMinute = minute;
      /* record prefs */
      SharedPreferences prefs = getSharedPreferences(appName, 0);
      SharedPreferences.Editor editor = prefs.edit();
      editor.putInt("alarmHour", mAlarmHour);
      editor.putInt("alarmMinute", mAlarmMinute);
      editor.commit();
      alarmTimePref.setSummary(AndroidUtils.pad(mAlarmHour, mAlarmMinute));
    }
  };

  private int mAlarmHour;
  private int mAlarmMinute;
  private CheckBoxPreference alarmStatePref;
  private Preference alarmTimePref;

  /**
   * @see android.app.Activity#onCreateDialog(int)
   */
  @Override
  protected Dialog onCreateDialog(int id)
  {
    switch (id)
    {
    case TIME_DIALOG_ID:
      TimePickerDialog timePickerDialog = new TimePickerDialog(this, mTimeSetListener,
          mAlarmHour, mAlarmMinute, true);

      return timePickerDialog;
    }
    return null;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    if (Log.DEBUG)
    {
      Log.v("HappyContactsPreferences: start");
    }
    SharedPreferences prefs = getSharedPreferences(appName, 0);
    mAlarmHour = prefs.getInt("alarmHour", AlarmController.DEFAULT_ALARM_HOUR);
    mAlarmMinute = prefs.getInt("alarmMinute", AlarmController.DEFAULT_ALARM_MINUTE);
    setPreferenceScreen(createPreferenceHierarchy());
  }

  private PreferenceScreen createPreferenceHierarchy()
  {
    // Root
    PreferenceScreen root = getPreferenceManager().createPreferenceScreen(this);

    // State
    alarmStatePref = new CheckBoxPreference(this);
    alarmStatePref.setTitle(R.string.pref_state);

    if (AlarmController.isAlarmUp(this))
    {
      alarmStatePref.setSummary(R.string.pref_state_on);
      alarmStatePref.setChecked(true);
    }
    else
    {
      alarmStatePref.setSummary(R.string.pref_state_off);
      alarmStatePref.setChecked(false);
    }
    alarmStatePref.setOnPreferenceClickListener(mAlarmToggleClickListener);
    root.addPreference(alarmStatePref);

    alarmTimePref = new Preference(this);
    alarmTimePref.setTitle(R.string.pref_time);
    alarmTimePref.setSummary(AndroidUtils.pad(mAlarmHour, mAlarmMinute));
    alarmTimePref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
    {
      @Override
      public boolean onPreferenceClick(Preference preference)
      {
        showDialog(TIME_DIALOG_ID);
        return true;
      }
    });
    root.addItemFromInflater(alarmTimePref);

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
    //    PreferenceCategory templatesPrefCat = new PreferenceCategory(this);
    //    templatesPrefCat.setTitle(R.string.pref_templates_cat);
    //    root.addPreference(templatesPrefCat);
    //
    //    EditTextPreference templateSmsPref = new EditTextPreference(this);
    //    templateSmsPref.setDialogTitle(R.string.pref_template_sms_dialog);
    //    templateSmsPref.setKey("templateSmsPref");
    //    templateSmsPref.setTitle(R.string.pref_template_sms);
    //    templateSmsPref.setSummary(R.string.pref_template_sms_summary);
    //    templatesPrefCat.addPreference(templateSmsPref);
    //
    //    EditTextPreference templateEmailPref = new EditTextPreference(this);
    //    templateEmailPref.setDialogTitle(R.string.pref_template_email_dialog);
    //    templateEmailPref.setKey("templateEmailPref");
    //    templateEmailPref.setTitle(R.string.pref_template_email);
    //    templateEmailPref.setSummary(R.string.pref_template_email_summary);
    //    templatesPrefCat.addPreference(templateEmailPref);

    return root;
  }
}
