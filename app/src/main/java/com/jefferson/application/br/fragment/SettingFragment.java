package com.jefferson.application.br.fragment;
import android.content.*;
import android.content.pm.*;
import android.os.*;
import android.preference.*;
import android.support.v4.app.*;
import android.support.v7.app.*;
import android.support.v7.widget.*;
import android.view.*;
import android.widget.*;
import android.widget.AdapterView.*;
import com.jefferson.application.br.*;
import com.jefferson.application.br.activity.*;
import com.jefferson.application.br.adapter.*;
import com.jefferson.application.br.model.*;
import java.util.*;
import com.jefferson.application.br.R.id.*;
import android.support.v7.widget.Toolbar;
import com.jefferson.application.br.util.*;

public class SettingFragment extends Fragment implements OnItemClickListener {

	public String[] storages;
	public String version;
	public final static String dev_mode = "modo_desenvolvedor";

    SettingAdapter mAdapter;
	Toolbar mToolbar;
	PackageInfo pInfo;
	SharedPreferences mShared;
	SharedPreferences.Editor mEdit;
	int count;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.config, null);
		mToolbar = (Toolbar) view.findViewById(R.id.toolbar);
		storages = new String[]{getString(R.string.armaz_interno),getString(R.string.armaz_externo)};

		((MainActivity)getActivity()).setupToolbar(mToolbar, getString(R.string.configuracoes));

		mShared = PreferenceManager.getDefaultSharedPreferences(getContext());
		mEdit = mShared.edit();

		ListView mListView = (ListView)view.findViewById(R.id.list_config);
		mListView.setDivider(null);

		try {
			pInfo = getContext().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
		} catch (PackageManager.NameNotFoundException e) {}

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
					item.item_name = "Esconder icone";
					item.icon_id = R.drawable.ic_android;
					item.type = PreferenceItem.ITEM_SWITCH_TYPE;
					item.description = getString(R.string.ocultar_descricao);
					break;
				case 6:
					item.item_name = "Código de chamada";
					item.type = item.ITEM_TYPE;
					item.icon_id = R.drawable.ic_dialpad;
					item.description = getCallCode();
					break;
				case 7:
					item.item_name = getString(R.string.preferecias_sobre);
					item.type = item.SECTION_TYPE;
					break;
				case 8:
					item.icon_id = R.drawable.ic_about;
					item.item_name = getString(R.string.app_name);
					item.type = item.ITEM_TYPE;
					break;
			}
			items.add(item);
		}
		mAdapter = new SettingAdapter(items, this);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);
		return view;
	}

	private String getCallCode() {
		return mShared.getString("secret_code", "#4321");

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
				Switch mSwitch = (Switch) view.findViewById(R.id.my_switch);
				boolean isChecked = !mSwitch.isChecked();
				/*if(isChecked && !mShared.getBoolean("dont_show_info_on_hidden", false)) {
					showWarning();
					break;
				}*/
				changeIconVisibility(isChecked);
				mSwitch.setChecked(isChecked);
				break;
			case 4:
				((MainActivity)getActivity()).showDialogChoose();
				break;
			case 6:
				changeCodeDialog();
				break;
			case 8:
				showAbout();
				break;
		}
	}

	private void changeIconVisibility(boolean isChecked) {
		getActivity().getPackageManager().setComponentEnabledSetting(new ComponentName(getContext(), "com.jefferson.application.br.LuancherAlias"), 
																	 isChecked ? PackageManager.COMPONENT_ENABLED_STATE_DISABLED: PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
	}
	public void changeCodeDialog() {

		final View view = getLayoutInflater(null).inflate(R.layout.dialog_call, null);
		final EditText editText = (EditText) view.findViewById(R.id.editTextDialogUserInput);
		editText.append(getCallCode());
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setTitle("Novo código");
		builder.setPositiveButton(getString(R.string.salvar), new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface p1, int p2) {
					String code = editText.getText().toString();
					if(code.length() < 3){
						Toast.makeText(getContext(), "O comprimento do código não pode ser menor que 3.", 1).show();
					} else if(code.length() > 15) {
						Toast.makeText(getContext(), "O código não pode ter mais que 15 de comprimento.",1).show();
					} else {
						mEdit.putString("secret_code", code).commit();
						mAdapter.getItem(6).description = code;
						mAdapter.notifyDataSetChanged();
					}
				}
			});
		builder.setNegativeButton(getString(R.string.cancelar), null);
		builder.setView(view);
		builder.show();
	}
	public int getComponentEnabledSetting() {
		return getActivity().getPackageManager().getComponentEnabledSetting(new ComponentName(getContext(), "com.jefferson.application.br.LuancherAlias"));
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
		AlertDialog.Builder build = new AlertDialog.Builder(getContext());
		build.setView(LayoutInflater.from(getContext()).inflate(R.layout.about, null, false));
		build.setPositiveButton("fechar", null);
		build.create().show();
	}
	private void showWarning() {
		View view = getLayoutInflater(null).inflate(R.layout.dialog_check_box_view, null);
		final CheckBox mCheckBox = (CheckBox) view.findViewById(R.id.dialogcheckbox);
		new AlertDialog.Builder(getContext())
		.setTitle("Informação")
		.setIcon(R.drawable.ic_information)
		.setMessage(String.format("Vc pode abriar a aplicativo efetuando uma chamanda para o código %s", getCallCode()))
		.setPositiveButton("fechar", null)
		.setView(view)
			.show().setOnDismissListener(new DialogInterface.OnDismissListener(){

				@Override
				public void onDismiss(DialogInterface dInterface) {
					if(mCheckBox.isChecked()) {
						mEdit.putBoolean("dont_show_info_on_hidden", true);
					}
					
				}
		});
	}
	private void showDialog() {
		final CharSequence[] itens={"Português(Brasil)","English","Español"};
		AlertDialog.Builder b = new AlertDialog.Builder(getContext())
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
					intent.setAction(MainActivity.ACTION_INIT_WITH_PREFERENCES);
					startActivity(intent);
					getActivity().finish();
				}});
		b.create().show();
	}
}
