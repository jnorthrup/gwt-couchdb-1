package com.os.couchdbjs.client.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;

public class BaseModel {
	protected JSOModel m_data;

	public BaseModel(JSOModel data) {
		this.m_data = data;
	}
	
	public BaseModel() {
		m_data = JSOModel.create();
	}

	public String get(String field) {
		String val = this.m_data.get(field);
		if (null != val && "null".equals(val) || "undefined".equals(val)) {
			return null;
		} else {
			return escapeHtml(val);
		}
	}

	public JSOModel getModel() {
		return m_data;
	}

	public Map<String, String> getFields() {
		Map<String, String> fieldMap = new HashMap<String, String>();
		if (null != m_data) {
			JsArrayString array = m_data.keys();
			for (int i = 0; i < array.length(); i++) {
				fieldMap.put(array.get(i), m_data.get(array.get(i)));
			}
		}
		return fieldMap;
	}

	public <T extends BaseModel> Map<String, T> getObjectMap(String key,BaseModelFactory<T> pFactory) {
		JSOModel attModel = m_data.getObject(key);
		if (null != attModel) {
			JsArrayString keys = attModel.keys();
			Map<String, T> result = new HashMap<String, T>(keys.length());
			for (int i =0;i < keys.length();i++) {
				JSOModel jsoValue = attModel.getObject(keys.get(i));
				if(null != jsoValue) {
					result.put(keys.get(i), pFactory.createInstance(jsoValue));
				} else {
					result.put(keys.get(i), null);
				}
			}
			return result;
		} else {
			return null;
		}
	}

	public <T extends BaseModel> T getObjectMapValue(String key,String name,BaseModelFactory<T> pFactory) {
		JSOModel attModel = m_data.getObject(key);
		if (null != attModel) {
			return pFactory.createInstance(attModel.getObject(name));
		} else {
			return null;
		}
	}
	
	public <T extends BaseModel> void setObjectMap(String pKey, Map<String, T> pMap) {
		if(null == pMap || pMap.isEmpty()) {
			getModel().unset(pKey);
		} else {
			JSOModel jsoModel = JSOModel.create();
			for (Map.Entry<String, T> entry : pMap.entrySet()) {
				jsoModel.set(entry.getKey(), entry.getValue().getModel());
			}
			getModel().set(pKey, jsoModel);
		}
	}

	public <T extends BaseModel> void addKeyValue(String pKey,String name, T pValue) {
		JSOModel attModel = getModel().getObject(pKey);
		if (null == attModel) {
			attModel = JSOModel.create();
			getModel().set(pKey, attModel);
		}
		attModel.set(name, pValue.getModel());
	}
	
	public <T extends BaseModel> List<T> getObjectList(String pKey,BaseModelFactory<T> pFactory) {
		List<T> result = new ArrayList<T>();
		JsArray<JSOModel> jsoArray = getModel().getArray(pKey);
		for(int i=0;i < jsoArray.length();i++) {
			JSOModel jsoValue = jsoArray.get(i);
			if(null != jsoValue) {
				result.add(pFactory.createInstance(jsoValue));
			} else {
				result.add(null);
			}
		}
		return result;
	}

	public <T extends BaseModel> void setObjectList(String pKey,List<T> pList) {
		JsArray<JSOModel> jsArray = JavaScriptObject.createArray().cast();
		for(int i = 0;i < pList.size();i++) {
			jsArray.set(i, pList.get(i).getModel());
		}
		getModel().set(pKey, jsArray);
	}
	
	public Map<String, String> getStringMap(String key) {
		JSOModel attModel = m_data.getObject(key);
		if (null != attModel) {
			JsArrayString keys = attModel.keys();
			Map<String, String> result = new HashMap<String, String>(keys.length());
			for (int i =0;i < keys.length();i++) {
				result.put(keys.get(i), attModel.get(keys.get(i)));
			}
			return result;
		} else {
			return null;
		}
	}

	public String getStringMapValue(String key,String name) {
		JSOModel attModel = m_data.getObject(key);
		if (null != attModel) {
			return attModel.get(name);
		} else {
			return null;
		}
	}
	
	public void setStringMap(String pKey, Map<String, String> pMap) {
		if(null == pMap || pMap.isEmpty()) {
			getModel().unset(pKey);
		} else {
			JSOModel jsoModel = JSOModel.create();
			for (Map.Entry<String, String> entry : pMap.entrySet()) {
				jsoModel.set(entry.getKey(), entry.getValue());
			}
			getModel().set(pKey, jsoModel);
		}
	}

	public void addKeyValue(String pKey,String name, String pValue) {
		JSOModel attModel = getModel().getObject(pKey);
		if (null == attModel) {
			attModel = JSOModel.create();
			getModel().set(pKey, attModel);
		}
		attModel.set(name, pValue);
	}

	public List<String> getStringList(String pKey) {
		List<String> result = new ArrayList<String>();
		JsArrayString jsoArray = getModel().getStringArray(pKey);
		for(int i=0;i < jsoArray.length();i++) {
			String jsoValue = jsoArray.get(i);
			if(null != jsoValue) {
				result.add(jsoValue);
			} else {
				result.add(null);
			}
		}
		return result;
	}

	public void setStringList(String pKey,List<String> pList) {
		JsArrayString jsArray = JavaScriptObject.createArray().cast();
		for(int i = 0;i < pList.size();i++) {
			jsArray.set(i, pList.get(i));
		}
		getModel().set(pKey, jsArray);
	}
	

	private static String escapeHtml(String maybeHtml) {
	  return maybeHtml;
	  /**
		final Element div = DOM.createDiv();
		DOM.setInnerText(div, maybeHtml);
		return DOM.getInnerHTML(div);
		*/
	}
	
	public static String toJson(BaseModel pModel) {
		return pModel.getModel().toJson();
	}
	
	public static String toJson(List<BaseModel> pValue) {
		JsArray<JSOModel> jsArray = JavaScriptObject.createArray().cast();
		for(int i=0;i < pValue.size();i++) {
			jsArray.set(i, pValue.get(i).getModel());
		}
		return JSOModel.toJson(jsArray);
	}
}