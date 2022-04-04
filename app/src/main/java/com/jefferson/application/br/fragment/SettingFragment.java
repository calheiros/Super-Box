package com.jefferson.application.br.fragment;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;
import com.jefferson.application.br.LocaleManager;
import com.jefferson.application.br.R;
import com.jefferson.application.br.activity.CreatePattern;
import com.jefferson.application.br.activity.MainActivity;
import com.jefferson.application.br.adapter.SettingAdapter;
import com.jefferson.application.br.model.PreferenceItem;
import com.jefferson.application.br.util.Storage;
import java.util.ArrayList;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Adapter;
import com.jefferson.application.br.util.JDebug;
import android.graphics.drawable.ColorDrawable;
import android.graphics.Color;
import android.widget.TextView;
import com.jefferson.application.br.util.ASCIIArt;
import java.net.URI;
import android.net.Uri;
import android.content.ActivityNotFoundException;
import android.view.View.OnClickListener;
import com.jefferson.application.br.util.DialogUtils;
import com.jefferson.application.br.activity.DeveloperActivity;

public class SettingFragment extends Fragment implements OnItemClickListener, OnClickListener, OnItemLongClickListener {

	public String[] storages;
	public String version;

    SettingAdapter mAdapter;
	Toolbar mToolbar;
	SharedPreferences mShared;
	SharedPreferences.Editor mEdit;
	int count;

    private int storageChoicePosition;

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

        for (int i = 0; i <= 8; i++) {
            PreferenceItem item = new PreferenceItem();
            switch (i) {
                case 0:
                    item.item_name = getString(R.string.preferecias_gerais);
                    item.type = item.SECTION_TYPE;
                    break;
                case 1:
                    item.icon_id = R.drawable.ic_key;
                    item.item_name = getString(R.string.mudar_senha);
                    item.type = item.ITEM_TYPE;
                    break;
                case 2:
                    item.icon_id = R.drawable.ic_language;
                    item.item_name = getString(R.string.idioma);
                    item.type = item.ITEM_TYPE;
                    item.description = getLanguage();
                    break;
                case 3:
                    item.type = item.SECTION_TYPE;
                    item.item_name = getString(R.string.preferecias_avancadas);
                    break;
                case 4:
                    item.type = item.ITEM_TYPE;
                    item.icon_id = R.drawable.ic_storage;
                    item.item_name = getString(R.string.local_armazenamento);
                    item.description = getStorageName();
                    break;
                case 5:
                    item.item_name = getString(R.string.modo_secreto);
                    item.icon_id = R.drawable.ic_drama_masks;
                    item.type = PreferenceItem.ITEM_SWITCH_TYPE;
                    item.description = getString(R.string.ocultar_descricao);
                    break;
                case 6:
                    item.item_name = getString(R.string.codigo_discador);
                    item.type = item.ITEM_TYPE;
                    item.icon_id = R.drawable.ic_dialpad;
                    item.description = getDialerCode();
                    break;
                case 7:
                    item.item_name = getString(R.string.preferecias_sobre);
                    item.type = item.SECTION_TYPE;
                    break;
                case 8:
                    item.icon_id = R.drawable.ic_about;
                    item.item_name = getString(R.string.app_name);
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

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
        if (position == 8) {
            Intent intent = new Intent(getContext(), DeveloperActivity.class);
            getActivity().startActivity(intent);
            return true;
        }
        return false;
    }

	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
		switch (position) {

			case 1:
				Intent intent = new Intent(getContext(), CreatePattern.class);
				intent.setAction(CreatePattern.ENTER_RECREATE);
				getActivity().startActivity(intent);
				break;
			case 2:
				showDialog();
				break;
			case 5:
				Switch mySwitch = (Switch) view.findViewById(R.id.my_switch);
				boolean checked = !mySwitch.isChecked();
                changeIconVisibility(checked);
				mySwitch.setChecked(checked);
				break;
			case 4:
				showDialogChoose();
				break;
			case 6:
				changeCodeDialog();
				break;
			case 8:
				showAbout();
				break;
		}
	}

    @Override
    public void onClick(View view) {
        openGithub();
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

    public void showDialogChoose() {
        final int storagePosition = Storage.getStoragePosition();
        storageChoicePosition = storagePosition;

        String[] options = new String[]{getString(R.string.armaz_interno), getString(R.string.armaz_externo)};
        if (Storage.getExternalStorage() == null)
            options = new String[]{getString(R.string.armaz_interno)};

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.CustomAlertDialog);
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
                        mainFragment.updateAll();
                    updateItem(4);
                }
            }
        );
        builder.setNegativeButton(getString(R.string.cancelar), null);
        configureRoundedDialog(builder.show());

	}

	private void changeIconVisibility(boolean isChecked) {
		getActivity().getPackageManager().setComponentEnabledSetting(new ComponentName(getContext(), "com.jefferson.application.br.LuancherAlias"), 
																	 isChecked ? PackageManager.COMPONENT_ENABLED_STATE_DISABLED: PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
	}

	public void changeCodeDialog() {
        final View view = getLayoutInflater(null).inflate(R.layout.dialog_call, null);
		final EditText editText = (EditText) view.findViewById(R.id.editTextDialogUserInput);
		editText.append(getDialerCode());

		AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.CustomAlertDialog);
		builder.setTitle("Novo código");
		builder.setPositiveButton(getString(R.string.salvar), new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface p1, int p2) {

                    String code = editText.getText().toString();

                    if (code.length() < 3) {
						Toast.makeText(getContext(), "O Código não pode ser menor que 3 caractéres.", 1).show();
					} else if (code.length() > 15) {
						Toast.makeText(getContext(), "O código não pode ter maior que 15 caractéres.", 1).show();
					} else {
						mEdit.putString("secret_code", code).commit();
						mAdapter.getItem(6).description = code;
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

    public void updateItem(int position) {
		mAdapter.getItem(position).description = getStorageName();
		mAdapter.notifyDataSetChanged();
	}

    private String getLanguage() {

        String locale = LocaleManager.getLanguage(getContext());

        if (locale == null)
			return "Padr\u00e3o do sistema";

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
        AlertDialog.Builder build = new AlertDialog.Builder(getActivity(), R.style.CustomAlertDialog);
		View view = LayoutInflater.from(getContext()).inflate(R.layout.credits_layout, null, false);
        TextView asciiView = view.findViewById(R.id.ascii_text_view);

        view.findViewById(R.id.githubTextView).setOnClickListener(this);
        asciiView.setText(ASCIIArt.CHIKA_ART);
		build.setView(view);
        build.setPositiveButton("fechar", null);
        configureRoundedDialog(build.show());
		//build.show().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
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

	private void showDialog() {

        final CharSequence[] itens = {"Português(Brasil)","English","Español"};

        AlertDialog.Builder b = new AlertDialog.Builder(getActivity(), R.style.CustomAlertDialog)
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
					LocaleManager.setNewLocale(getContext(), locale);
					Intent intent = new Intent(getContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
					intent.setAction(MainActivity.ACTION_INIT_WITH_PREFERENCES);
					startActivity(intent);
				}
            }
        );
		configureRoundedDialog(b.show());
	}
}
