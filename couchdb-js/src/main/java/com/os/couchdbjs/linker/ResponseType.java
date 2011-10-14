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
* Time: 11:40 PM
*/
public enum ResponseType {
  map ("function(doc) {"                         , "global_gwt.map(doc);"                                        ){ @Override void fire(CouchDBLinker couchDBLinker, String m_name, Database db, DesignDocument dd, String script, TreeLogger pLogger) throws UnableToCompleteException { View view = dd.getView(m_name); if (view == null) view = new View(script); else view.setMap(script); dd.getViews().put(m_name, view); db.createOrUpdateDocument(dd); } @Override public String ppath(String m_designName, String m_name) { return m_designName + "/views/" + m_name + "/map.js"; } },
  reduce("function(keys,values) {"               , "return global_gwt.reduce(keys,values);"                        ) { @Override public String ppath(String m_designName, String m_name) { return m_designName + "/views/" + m_name + "/reduce.js"; } @Override void fire(CouchDBLinker couchDBLinker, String m_name, Database db, DesignDocument dd, String script, TreeLogger pLogger) throws UnableToCompleteException { View view = dd.getView(m_name); if (view == null) { pLogger.log(TreeLogger.Type.ERROR, "Cannot have reduce without map for " + m_name); throw new UnableToCompleteException(); } else view.setReduce(script); dd.getViews().put(m_name, view); db.createOrUpdateDocument(dd); } },
  show ("function(doc,req) {"                    , "return global_gwt.show(doc,req);"                              ){ @Override public String ppath(String m_designName, String m_name) { return m_designName + "/shows/" + m_name + ".js"; } @Override void fire(CouchDBLinker couchDBLinker, String m_name, Database db, DesignDocument dd, String script, TreeLogger pLogger) { dd.addShowFunction(m_name, script); db.createOrUpdateDocument(dd); } },
  list ("function(head,req) {"                   , "return global_gwt.list(head,req);"                          ){ @Override public String ppath(String m_designName, String m_name) { return m_designName + "/lists/" + m_name + ".js"; } @Override void fire(CouchDBLinker couchDBLinker, String m_name, Database db, DesignDocument dd, String script, TreeLogger pLogger) { dd.addListFunction(m_name, script); db.createOrUpdateDocument(dd); } },
  filter ("function(doc,req) {"                  , "return global_gwt.filter(doc,req);"                             ){ @Override public String ppath(String m_designName, String m_name) { return m_designName + "/filters/" + m_name + ".js"; } @Override void fire(CouchDBLinker couchDBLinker, String m_name, Database db, DesignDocument dd, String script, TreeLogger pLogger) { Map<String, String> ft = (Map<String, String>) dd.getProperty("filters"); if (ft == null) { ft = new HashMap<String, String>(); dd.setProperty("filters", ft); } ft.put(m_name, script); db.createOrUpdateDocument(dd); } },
  validate ("function(newDoc,oldDoc,userCtx) {"  , "global_gwt.validate(newDoc,oldDoc,userCtx);"                              ){   @Override public String ppath(String m_designName, String m_name) { return m_name + "/validat_doc_update.js"; } @Override void fire(CouchDBLinker couchDBLinker, String m_name, Database db, DesignDocument dd, String script, TreeLogger pLogger) { dd.setValidateOnDocUpdate(script); db.createOrUpdateDocument(dd); } },
  fulltext ("function(doc) {"                    , "return global_gwt.fulltext(doc);"                                               ){ @Override public String ppath(String m_designName, String m_name) { return m_designName + "/fulltext/" + m_name + ".js"; } @Override void fire(CouchDBLinker couchDBLinker, String m_name, Database db, DesignDocument dd, String script, TreeLogger pLogger) { Map<String, Map<String, String>> ft = (Map<String, Map<String, String>>) dd.getProperty("fulltext"); if (ft == null) { ft = new HashMap<String, Map<String, String>>(); dd.setProperty("fulltext", ft); } Map<String, String> sm = ft.get(m_name); if (sm == null) { sm = new HashMap<String, String>(); ft.put(m_name, sm); } sm.put("index", script); db.createOrUpdateDocument(dd); } },
  update("function(doc,req) {"                   , "return global_gwt.update(doc,req);"                                     ) {   @Override public String ppath(String m_designName, String m_name) { return m_designName + "/update/" + m_name + ".js"; } @Override void fire(CouchDBLinker couchDBLinker, String m_name, Database db, DesignDocument dd, String script, TreeLogger pLogger) { Map<String, String> ft = (Map<String, String>) dd.getProperty("updates"); if (ft == null) { ft = new HashMap<String, String>(); dd.setProperty("updates", ft); } ft.put(m_name, script); db.createOrUpdateDocument(dd); } }; protected String stopen;
  public String stclose;



  ResponseType(String stopen, String
      stclose) {
    //To change body of created methods use File | Settings | File Templates.
    this.stopen = stopen;
    this.stclose = stclose;
  }

  abstract void fire(CouchDBLinker couchDBLinker, String m_name, Database db, DesignDocument dd, String script, TreeLogger pLogger) throws UnableToCompleteException;


  public abstract String ppath(String m_designName, String m_name);
}
