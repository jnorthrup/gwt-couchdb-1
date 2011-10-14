package com.os.couchdbjs.client.model;

import java.util.Date;

import com.google.gwt.core.client.JavaScriptObject;

public class ValueRow extends BaseModel {
  private Object m_value;
  private Object m_key;

  private static <T> T getJavaValueFromJsValue(JSOModel pModel, String pKey) {
    String type = pModel.getType(pKey);
    try {
      return ValueRowTypeMeta.valueOf(type + '$').fire(pModel, pKey);
    } catch (IllegalArgumentException e) {


      return (T) JSOModel.fromJavascriptObject(pModel.getNativeValue(pKey));
    }
  }

  public ValueRow(JSOModel pModel) {
    super(pModel);
  }

  public String getId() {
    return super.get("id");
  }

  public <T> T getKey() {
    if (m_key == null) {
      m_key = getJavaValueFromJsValue(getModel(), "key");
    }
    return (T) m_key;
  }

  /**
   * Returns the value mapped to this row.
   */
  public <T> T getValue() {
    if (m_value == null) {
      m_value = getJavaValueFromJsValue(getModel(), "value");
    }
    return (T) m_value;
  }

  /**
  * User: jim
  * Date: 10/13/11
  * Time: 10:54 PM
  */
    enum ValueRowTypeMeta {
    string$ {
      @Override
      Object fire(JSOModel pModel, String pKey) {
        return pModel.get(pKey);
      }
    },
    number$ {
      @Override
      Double fire(JSOModel pModel, String pKey) {
        return pModel.getDouble(pKey);
      }
    },
    boolean$ {
      @Override
      Boolean fire(JSOModel pModel, String pKey) {
        return pModel.getBoolean(pKey) ? Boolean.TRUE : Boolean.FALSE;
      }
    },
    date$ {
      @Override
      Date fire(JSOModel pModel, String pKey) {
        return pModel.getDate(pKey);
      }
    },
    array$ {
      @Override
      JavaScriptObject fire(JSOModel pModel, String pKey) {
        return pModel.getNativeValue(pKey);
      }
    },
    null$ {
      @Override
      <T> T fire(JSOModel pModel, String pKey) {
        return null;
      }
    },
    undefined$ {
      @Override
      <T> T
      fire(JSOModel pModel, String pKey) {
        return null;
      }
    };

    abstract <T> T fire(JSOModel pModel, String pKey);
  }
}