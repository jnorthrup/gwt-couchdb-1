package com.os.couchdbjs.linker;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import org.jcouchdb.db.Database;
import org.jcouchdb.document.DesignDocument;
import org.jcouchdb.document.View;

/**
 * User: jim
 * Date: 10/13/11
 * Time: 9:03 PM
 */
enum MsgType {
  map {
    @Override
    void fire(CouchDBLinker couchDBLinker, String m_name, Database db, DesignDocument dd, String script, TreeLogger pLogger) throws UnableToCompleteException {
               View view = dd.getView(m_name);
            if (view == null) {
              view = new View(script);
            } else {
              view.setMap(script);
            }
            dd.getViews().put(m_name, view);
            db.createOrUpdateDocument(dd);
    }
  },
  reduce {
    @Override
    void fire(CouchDBLinker couchDBLinker, String m_name, Database db, DesignDocument dd, String script, TreeLogger pLogger) throws UnableToCompleteException {
            View view = dd.getView(m_name);
            if (view == null) {
              pLogger.log(TreeLogger.Type.ERROR, "Cannot have reduce without map for " + m_name);
              throw new UnableToCompleteException();
            } else {
              view.setReduce(script);
            }
            dd.getViews().put(m_name, view);
            db.createOrUpdateDocument(dd);
    }
  },
  show {
    @Override
    void fire(CouchDBLinker couchDBLinker, String m_name, Database db, DesignDocument dd, String script, TreeLogger pLogger) {

       dd.addShowFunction(m_name, script);
    db.createOrUpdateDocument(dd);}
  },
  list {
    @Override
    void fire(CouchDBLinker couchDBLinker, String m_name, Database db, DesignDocument dd, String script, TreeLogger pLogger) {
       dd.addListFunction(m_name, script);
    db.createOrUpdateDocument(dd);   //todo: verify for a purpose
    }
  },
  filter {
    @Override
    void fire(CouchDBLinker couchDBLinker, String m_name, Database db, DesignDocument dd, String script, TreeLogger pLogger) {

            Map<String, String> ft = (Map<String, String>) dd.getProperty("filters");
            if (ft == null) {
              ft = new HashMap<String, String>();
              dd.setProperty("filters", ft);
            }
            ft.put(m_name, script);
            db.createOrUpdateDocument(dd);
    }
  },
  validate {
    @Override
    void fire(CouchDBLinker couchDBLinker, String m_name, Database db, DesignDocument dd, String script, TreeLogger pLogger) {

            dd.setValidateOnDocUpdate(script);
            db.createOrUpdateDocument(dd);
    }
  },
  fulltext {
    @Override
    void fire(CouchDBLinker couchDBLinker, String m_name, Database db, DesignDocument dd, String script, TreeLogger pLogger) {
         Map<String, Map<String, String>> ft = (Map<String, Map<String, String>>) dd.getProperty("fulltext");
            if (ft == null) {
              ft = new HashMap<String, Map<String, String>>();
              dd.setProperty("fulltext", ft);
            }
            Map<String, String> sm = ft.get(m_name);
            if (sm == null) {
              sm = new HashMap<String, String>();
              ft.put(m_name, sm);
            }
            sm.put("index", script);
            db.createOrUpdateDocument(dd);
    }
  },
  update {
    @Override
    void fire(CouchDBLinker couchDBLinker, String m_name, Database db, DesignDocument dd, String script, TreeLogger pLogger) {
       Map<String, String> ft = (Map<String, String>) dd.getProperty("updates");
            if (ft == null) {
              ft = new HashMap<String, String>();
              dd.setProperty("updates", ft);
            }
            ft.put(m_name, script);
            db.createOrUpdateDocument(dd);
    }
  };

  abstract void fire(CouchDBLinker couchDBLinker, String m_name, Database db, DesignDocument dd, String script, TreeLogger pLogger) throws UnableToCompleteException;
}
