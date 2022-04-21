package com.jefferson.application.br.fragment;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import com.jefferson.application.br.LocaleManager;
import com.jefferson.application.br.R;
import com.jefferson.application.br.activity.CreatePattern;
import com.jefferson.application.br.activity.DeveloperActivity;
import com.jefferson.application.br.activity.MainActivity;
import com.jefferson.application.br.adapter.SettingAdapter;
import com.jefferson.application.br.app.SimpleDialog;
import com.jefferson.application.br.model.PreferenceItem;
import com.jefferson.application.br.util.ASCIIArt;
import com.jefferson.application.br.util.DialogUtils;
import com.jefferson.application.br.util.MyPreferences;
import com.jefferson.application.br.util.Storage;
import java.util.ArrayList;
import com.jefferson.application.br.util.MyAnimationUtils;
import com.jefferson.application.br.util.ThemeUtils;

public class SettingFragment extends Fragment implements OnItemClickListener, OnClickListener, OnItemLongClickListener {

	public String[] storages;
	public String version;
    private SettingAdapter mAdapter;
	private Toolbar mToolbar;
	private SharedPreferences mShared;
	private SharedPreferences.Editor mEdit;
	private int egg;
    private int storageChoicePosition;
    public static final int CALCULATOR_CREATE_CODE_RESULT = 85;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.config, null);
		mToolbar = (Toolbar) view.findViewById(R.id.toolbar);
		storages = new String[]{ getString(R.string.armaz_interno), getString(R.string.armaz_externo) };

		((MainActivity)getActivity()).setupToolbar(mToolbar, getString(R.string.configuracoes));

		mShared = PreferenceManager.getDefaultSharedPreferences(getContext());
		mEdit = mShared.edit();
		ListView mListView = (ListView)view.findViewById(R.id.list_config);
		mListView.setDivider(null);

        ArrayList<PreferenceItem> items = createItemsList();
		mAdapter = new SettingAdapter(items, this);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);
        mListView.setOnItemLongClickListener(this);
		return view;
	}

	private String getDialerCode() {
		return mShared.getString("secret_code", "#4321");
	}

    public ArrayList<PreferenceItem> createItemsList() {
        ArrayList<PreferenceItem> items = new ArrayList<>();

        for (int i = 0; i <= 9; i++) {
            PreferenceItem item = new PreferenceItem();
            switch (i) {
                case 0:
                    item.title = getString(R.string.preferecias_gerais);
                    item.type = item.SECTION_TYPE;
                    break;
                case 1:
                    item.id = PreferenceItem.ID.PASSWORD;
                    item.icon_id = R.drawable.ic_key;
                    item.title = getString(R.string.mudar_senha);
                    item.type = item.ITEM_TYPE;
                    break;
                case 2:
                    item.id = PreferenceItem.ID.LANGUAGE;
                    item.icon_id = R.drawable.ic_language;
                    item.title = getString(R.string.idioma);
                    item.type = item.ITEM_TYPE;
                    item.description = getLanguage();
                    break;
                case 3:
                    item.id = PreferenceItem.ID.APP_THEME;
                    item.type = PreferenceItem.ITEM_TYPE;
                    item.icon_id = R.drawable.ic_palette;
                    item.title = "App theme";
                    break;
                case 4:
                    item.type = item.SECTION_TYPE;
                    item.title = getString(R.string.preferecias_avancadas);
                    break;
                case 5:
                    item.id  = PreferenceItem.ID.STORAGE;
                    item.type = item.ITEM_TYPE;
                    item.icon_id = R.drawable.ic_storage;
                    item.title = getString(R.string.local_armazenamento);
                    item.description = getStorageName();
                    break;
                case 6:
                    item.id = PreferenceItem.ID.APP_ICON;
                    item.title = getString(R.string.modo_secreto);
                    item.icon_id = R.drawable.ic_drama_masks;
                    item.type = PreferenceItem.ITEM_SWITCH_TYPE;
                    item.description = getString(R.string.ocultar_descricao);
                    break;
                case 7:
                    item.id = PreferenceItem.ID.DIALER_CODE;
                    item.title = getString(R.string.codigo_discador);
                    item.type = item.ITEM_TYPE;
                    item.icon_id = R.drawable.ic_dialpad;
                    item.description = getDialerCode();
                    break;
                case 8:
                    item.title = getString(R.string.preferecias_sobre);
                    item.type = item.SECTION_TYPE;
                    break;
                case 9:
                    item.id = PreferenceItem.ID.ABOUT;
                    item.icon_id = R.drawable.ic_about;
                    item.title = getString(R.string.app_name);
                    item.type = item.ITEM_TYPE;
                    try {
                        item.description = getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0).versionName;
                    } catch (PackageManager.NameNotFoundException e) {}
                    break;
            }
            items.add(item);
		}
        return items;
    }

    private void setAllEggsfound() {
        MyPreferences.getSharedPreferencesEditor().putBoolean("eggs_found", true).commit();
    }

    private boolean allEggsFound() {
        return MyPreferences.getSharedPreferences().getBoolean("eggs_found", false);
    }

    private void enterDebugActivitu() {
        Intent intent = new Intent(getContext(), DeveloperActivity.class);
        getActivity().startActivity(intent);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
        if (position == 8) {
            return true;
        }
        return false;
    }

	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
        PreferenceItem.ID itemId  = mAdapter.getItem(position).id;

        if (itemId == PreferenceItem.ID.PASSWORD) {
            Intent intent = new Intent(getContext(), CreatePattern.class);
            intent.setAction(CreatePattern.ENTER_RECREATE);
            getActivity().startActivity(intent);
        } else if (itemId == PreferenceItem.ID.LANGUAGE) {
            showLanguageDialog();
        } else if (itemId == PreferenceItem.ID.APP_THEME) {
            showThemeDialog();
        } else if (itemId == PreferenceItem.ID.STORAGE) {
            showDialogChooseStorage();
        } else if (itemId == PreferenceItem.ID.APP_ICON) {
            /* Switch mySwitch = (Switch) view.findViewById(R.id.my_switch);
             //            if (!mySwitch.isChecked() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
             //                SimpleDialog alert = new SimpleDialog(getContext());
             //                alert.setTitle("Unsupported!");
             //                alert.setMessage("This feature is no longer supported on Android 10 or later.");
             //                alert.setNegativeButton("okay", null);
             //                alert.show();
             //                return;
             //            }
             checked = !mySwitch.isChecked();
             View layout = view.findViewById(R.id.steal_thexpandable_layout);

             if (checked) {
             MyAnimationUtils.expand(layout);
             } else {
             MyAnimationUtils.collapse(layout);
             }

             changeIconVisibility(checked);
             mySwitch.setChecked(checked);*/
        } else if (itemId == PreferenceItem.ID.DIALER_CODE) {
            changeCodeDialog();
        } else if (itemId == PreferenceItem.ID.ABOUT) {
            showAbout();
        }
    }

    @Override
    public void onStop() {
        super.onStop();

    }

    private void showThemeDialog() {
        final CharSequence[] itens = ThemeUtils.THEME_LIST;
        AlertDialog.Builder b = new AlertDialog.Builder(getActivity(), DialogUtils.getTheme())
            .setTitle("Choose your theme")
            .setItems(itens, new DialogInterface.OnClickListener(){

                @Override
                public void onClick(DialogInterface p1, int position) {
                    int themeId = ThemeUtils.getThemeIndex();
                    
                    if (position != themeId) {
                        ThemeUtils.setThemeIndex(position);
                        refreshActivity();
                    }
                }
            }
        );
        configureRoundedDialog(b.show());
    }

    @Override
    public void onClick(View view) {
        openGithub();
    }

    @Override
    public void onPause() {
        super.onPause();
        egg = 0;
    }

    public void configureRoundedDialog(AlertDialog dialog) {
        DialogUtils.configureRoudedDialog(dialog);
    }

    public void openGithub() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.github.com/calheiros"));

        try {
            startActivity(intent);
        } catch (ActivityNotFoundException err) {
            Toast.makeText(getContext(), "Can't open URL", Toast.LENGTH_LONG).show();
        }
    }

    public void showDialogChooseStorage() {
        final int storagePosition = Storage.getStoragePosition();
        storageChoicePosition = storagePosition;

        String[] options = new String[]{getString(R.string.armaz_interno), getString(R.string.armaz_externo)};
        if (Storage.getExternalStorage() == null)
            options = new String[]{getString(R.string.armaz_interno)};

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), DialogUtils.getTheme());
        builder.setTitle(getString(R.string.armazenamento));
        builder.setSingleChoiceItems(options, storagePosition, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int position) {
                    storageChoicePosition = position;
                }
            }
        );

        builder.setPositiveButton(getString(R.string.salvar), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int p2) {

                    if (storageChoicePosition == storagePosition) {
                        return;
                    }
                    Storage.setNewLocalStorage(storageChoicePosition);

                    ((MainActivity) getActivity()).
                        mainFragment.updateAllFragments();
                    updateItemDescription(PreferenceItem.ID.STORAGE, getStorageName());
                }
            }
        );
        builder.setNegativeButton(getString(R.string.cancelar), null);
        configureRoundedDialog(builder.show());
	}

    private void refreshActivity() {
        Intent intent = new Intent(getContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.setAction(MainActivity.ACTION_START_IN_PREFERENCES);
        startActivity(intent);
        getActivity().overridePendingTransition(0, 0);
    }

	private void changeIconVisibility(boolean isChecked) {
		getActivity().getPackageManager().setComponentEnabledSetting(new ComponentName(getContext(), "com.jefferson.application.br.LuancherAlias"), 
																	 isChecked ? PackageManager.COMPONENT_ENABLED_STATE_DISABLED: PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
	}

    private void setCompomentEnabled(boolean enabled, String component) {
        getActivity().getPackageManager().setComponentEnabledSetting(new ComponentName(getContext(), component),
                                                                     enabled ? PackageManager.COMPONENT_ENABLED_STATE_DISABLED: PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }

	public void changeCodeDialog() {
        final View view = getLayoutInflater(null).inflate(R.layout.dialog_call, null);
		final EditText editText = (EditText) view.findViewById(R.id.editTextDialogUserInput);
		editText.append(getDialerCode());

		AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), DialogUtils.getTheme());
		builder.setTitle("New code");
		builder.setPositiveButton(getString(R.string.salvar), new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface p1, int p2) {

                    String code = editText.getText().toString();

                    if (code.length() < 3) {
						Toast.makeText(getContext(), "O Código não pode ser menor que 3 caractéres.", 1).show();
					} else if (code.length() > 15) {
						Toast.makeText(getContext(), "O código não pode ter mais que 15 caractéres.", 1).show();
					} else {
						mEdit.putString("secret_code", code).commit();
						updateItemDescription(PreferenceItem.ID.DIALER_CODE, code);
						mAdapter.notifyDataSetChanged();
					}
				}
			}
        );

		builder.setNegativeButton(getString(R.string.cancelar), null);
		builder.setView(view);
        configureRoundedDialog(builder.show());
	}

	public int getComponentEnabledSetting() {
		return getActivity().getPackageManager().getComponentEnabledSetting(new ComponentName(getContext(), getActivity().getPackageName() + ".LuancherAlias"));
	}

	private String getStorageName() {
		return Storage.getStorageLocation().equals(Storage.INTERNAL) ? getString(R.string.armaz_interno) : getString(R.string.armaz_externo);
    }

    public void updateItemDescription(PreferenceItem.ID id, String description) {

        for (int i = 0; i < mAdapter.getCount(); i++) {
            if (mAdapter.getItem(i).id == id) {
                mAdapter.getItem(i).description = description;
                mAdapter.notifyDataSetChanged();
                break;
            }
        }
	}

    private String getLanguage() {
        String locale = LocaleManager.getLanguage(getContext());

        if (locale == null)
			return getString(R.string.padrao_do_sistema);

		switch (locale) {
			case "en":
				return "English";
			case "pt":
				return "Portugu\u00eas";
			case "es":
				return "Espa\u00f1ol";
        }
		return null;
    }

	private void showAbout() {
        AlertDialog.Builder build = new AlertDialog.Builder(getActivity(), DialogUtils.getTheme());
		View view = LayoutInflater.from(getContext()).inflate(R.layout.credits_layout, null, false);
        TextView asciiTextView = view.findViewById(R.id.ascii_text_view);
        Typeface font = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Raleway-Regular.ttf");

        asciiTextView.setTypeface(font);
        asciiTextView.setLetterSpacing(0);
        asciiTextView.setText(ASCIIArt.CHIKA_ART);
        view.findViewById(R.id.githubTextView).setOnClickListener(this);

		build.setView(view);
        build.setPositiveButton("fechar", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialogInterface, int id) {
                    dialogInterface.cancel();
                    egg = 0;
                }
            }
        );

        if (!allEggsFound()) {
            build.setOnDismissListener(new DialogInterface.OnDismissListener() {

                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        if (++egg == 7) {
                            setAllEggsfound();
                        }
                    }
                }
            );
        }

        if (allEggsFound()) {
            build.setNegativeButton("DEV", new DialogInterface.OnClickListener() {

                    @Override 
                    public void onClick(DialogInterface dialog, int id) {
                        enterDebugActivitu();
                    }
                }
            );
        }
        AlertDialog alert = build.show();
        alert.setCanceledOnTouchOutside(true);
        configureRoundedDialog(alert);
    }

	private void showWarning() {

        View view = getLayoutInflater(null).inflate(R.layout.dialog_check_box_view, null);
		final CheckBox mCheckBox = (CheckBox) view.findViewById(R.id.dialogcheckbox);
		new AlertDialog.Builder(getActivity())
            .setTitle("Informação")
            .setIcon(R.drawable.ic_information)
            .setMessage(String.format("Vc pode abriar a aplicativo efetuando uma chamanda para o código %s", getDialerCode()))
            .setPositiveButton("fechar", null)
            .setView(view)
			.show().setOnDismissListener(new DialogInterface.OnDismissListener(){

				@Override
				public void onDismiss(DialogInterface dInterface) {
					if (mCheckBox.isChecked()) {
						mEdit.putBoolean("dont_show_info_on_hidden", true);
					}
				}
            }
        );
	}

	private void showLanguageDialog() {

        final CharSequence[] itens = {"Português(Brasil)","English","Español"};

        AlertDialog.Builder b = new AlertDialog.Builder(getActivity(), DialogUtils.getTheme())
			.setTitle(R.string.escolha_idioma)
			.setItems(itens, new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface p1, int position) {
					String locale = null;
					switch (position) {
						case 0:
							locale = "pt";
							break;
						case 1:
							locale = "en";
							break;
						case 2:
							locale = "es";
							break;
					}
                    if (!locale.equals(LocaleManager.getLanguage(getContext()))) {
					    LocaleManager.setNewLocale(getContext(), locale);
                        refreshActivity();
                    }
                }
            }
        );
		configureRoundedDialog(b.show());
	}
}
