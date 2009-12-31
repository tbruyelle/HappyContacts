/**
 * 
 */
package com.kamosoft.happycontacts;

import java.text.SimpleDateFormat;
import java.util.Date;

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

import com.kamosoft.happycontacts.alarm.AlarmController;
import com.kamosoft.happycontacts.blacklist.BlackListActivity;
import com.kamosoft.happycontacts.dao.DbAdapter;
import com.kamosoft.utils.AndroidUtils;

/**
 * @author tom
 * 
 */
public class HappyContactsPreferences
    extends PreferenceActivity
    implements Constants
{
    private static final int TIME_DIALOG_ID = 0;

    /**
     * alarm click action
     */
    private OnPreferenceClickListener mAlarmToggleClickListener = new OnPreferenceClickListener()
    {
        public boolean onPreferenceClick( Preference preference )
        {
            SharedPreferences.Editor editor = mPrefs.edit();
            if ( AlarmController.isAlarmUp( HappyContactsPreferences.this ) )
            {
                AlarmController.cancelAlarm( HappyContactsPreferences.this );
                preference.setSummary( R.string.pref_alarm_off );
                editor.putBoolean( PREF_ALARM_STATUS, true );
            }
            else
            {
                AlarmController.startAlarm( HappyContactsPreferences.this );
                preference.setSummary( R.string.pref_alarm_on );
                editor.putBoolean( PREF_ALARM_STATUS, false );
            }
            editor.commit();
            return true;
        }
    };

    /**
     * hour click action
     */
    private TimePickerDialog.OnTimeSetListener mTimeSetListener = new TimePickerDialog.OnTimeSetListener()
    {
        public void onTimeSet( TimePicker view, int hourOfDay, int minute )
        {
            mAlarmHour = hourOfDay;
            mAlarmMinute = minute;
            /* record prefs */
            SharedPreferences.Editor editor = mPrefs.edit();
            editor.putInt( PREF_ALARM_HOUR, mAlarmHour );
            editor.putInt( PREF_ALARM_MINUTE, mAlarmMinute );
            editor.commit();
            mAlarmTimePref.setSummary( AndroidUtils.pad( mAlarmHour, mAlarmMinute ) );
            if ( AlarmController.isAlarmUp( HappyContactsPreferences.this ) )
            {
                /* if alarm is up, have to change it */
                AlarmController.startAlarm( HappyContactsPreferences.this );
            }
        }
    };

    private SharedPreferences mPrefs;

    private int mAlarmHour;

    private int mAlarmMinute;

    private Preference mAlarmTimePref;

    /**
     * @see android.app.Activity#onCreateDialog(int)
     */
    @Override
    protected Dialog onCreateDialog( int id )
    {
        switch ( id )
        {
            case TIME_DIALOG_ID:
                TimePickerDialog timePickerDialog =
                    new TimePickerDialog( this, mTimeSetListener, mAlarmHour, mAlarmMinute, true );

                return timePickerDialog;
        }
        return null;
    }

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        if ( Log.DEBUG )
        {
            Log.v( "HappyContactsPreferences: start" );
        }

        mPrefs = getSharedPreferences( APP_NAME, 0 );
        checkInit();
        mAlarmHour = mPrefs.getInt( PREF_ALARM_HOUR, AlarmController.DEFAULT_ALARM_HOUR );
        mAlarmMinute = mPrefs.getInt( PREF_ALARM_MINUTE, AlarmController.DEFAULT_ALARM_MINUTE );

        setPreferenceScreen( createPreferenceHierarchy() );

    }

    /**
     * check if some initializations have to be done
     */
    private void checkInit()
    {
        if ( mPrefs.getBoolean( PREF_FIRST_RUN, true ) )
        {
            /* if its the first run, alarm must be set */
            mPrefs.edit().putBoolean( PREF_FIRST_RUN, false ).commit();
            AlarmController.startAlarm( this );

            /* also we create the database with a random query to display a progress dialog */            
            new DbAdapter( this ).createOrUpdate();
        }
        else
        {
            /* check if need upgrade to do it and display a progress dialog */
            DbAdapter dbAdapter = new DbAdapter( this );
            if ( dbAdapter.needUpgrade() )
            {
                dbAdapter.createOrUpdate();
                //                ProgressDialog progressDialog =
                //                    ProgressDialog.show( this, this.getString( R.string.please_wait ),
                //                                         this.getString( R.string.updating_data ) );
                //                new DatabaseInitializer( this, progressDialog ).start();
            }
        }
    }

    /**
     * @return the built preferences
     */
    private PreferenceScreen createPreferenceHierarchy()
    {
        // Root
        PreferenceScreen root = getPreferenceManager().createPreferenceScreen( this );

        // State
        CheckBoxPreference alarmStatePref = new CheckBoxPreference( this );
        alarmStatePref.setTitle( R.string.pref_alarm );

        if ( AlarmController.isAlarmUp( this ) )
        {
            alarmStatePref.setSummary( R.string.pref_alarm_on );
            alarmStatePref.setChecked( true );
        }
        else
        {
            alarmStatePref.setSummary( R.string.pref_alarm_off );
            alarmStatePref.setChecked( false );
        }
        alarmStatePref.setOnPreferenceClickListener( mAlarmToggleClickListener );
        root.addPreference( alarmStatePref );

        Preference alarmTimePref = new Preference( this );
        mAlarmTimePref = alarmTimePref;
        alarmTimePref.setTitle( R.string.pref_time );
        alarmTimePref.setSummary( AndroidUtils.pad( mAlarmHour, mAlarmMinute ) );
        alarmTimePref.setOnPreferenceClickListener( new Preference.OnPreferenceClickListener()
        {
            public boolean onPreferenceClick( Preference preference )
            {
                showDialog( TIME_DIALOG_ID );
                return true;
            }
        } );
        root.addItemFromInflater( alarmTimePref );

        // test app
        PreferenceScreen testAppPref = getPreferenceManager().createPreferenceScreen( this );
        testAppPref.setTitle( R.string.pref_test_app );
        testAppPref.setSummary( R.string.pref_test_app_summary );
        Intent intent = new Intent( this, NameListActivity.class );
        SimpleDateFormat dateFormat = new SimpleDateFormat( DAY_FORMAT );
        intent.putExtra( DATE_INTENT_KEY, dateFormat.format( new Date() ) );
        testAppPref.setIntent( intent );
        root.addPreference( testAppPref );

        // blacklist
        PreferenceScreen blackListPref = getPreferenceManager().createPreferenceScreen( this );
        blackListPref.setTitle( R.string.pref_blacklist );
        blackListPref.setSummary( R.string.pref_blacklist_summary );
        blackListPref.setIntent( new Intent( this, BlackListActivity.class ) );
        root.addPreference( blackListPref );

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
