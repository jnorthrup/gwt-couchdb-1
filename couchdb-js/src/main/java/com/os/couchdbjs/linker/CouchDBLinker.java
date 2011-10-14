package com.os.couchdbjs.linker;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;

import com.google.gwt.core.ext.LinkerContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.linker.AbstractLinker;
import com.google.gwt.core.ext.linker.ArtifactSet;
import com.google.gwt.core.ext.linker.ConfigurationProperty;
import com.google.gwt.core.ext.linker.EmittedArtifact;
import com.google.gwt.core.ext.linker.LinkerOrder;
import com.google.gwt.core.ext.linker.LinkerOrder.Order;
import org.jcouchdb.db.Database;
import org.jcouchdb.db.Response;
import org.jcouchdb.document.DesignDocument;
import org.jcouchdb.document.View;
import org.jcouchdb.exception.NotFoundException;

@LinkerOrder(Order.POST)
public final class CouchDBLinker extends AbstractLinker {

  public enum ResponseType {
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
        db.createOrUpdateDocument(dd);
      }
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


  static enum SetupMeta {
    couchdb_name {
      @Override
      void fire(CouchDBLinker couchDBLinker, ConfigurationProperty prop) {
        String dbName = couchDBLinker.getCfgValue(prop);
        if (null != dbName) {
          couchDBLinker.m_dbName = dbName;
        }   //todo: verify for a purpose
      }
    },
    couchdb_host {
      @Override
      void fire(CouchDBLinker couchDBLinker, ConfigurationProperty prop) {
        String dbHost = couchDBLinker.getCfgValue(prop);
        if (null != dbHost) {
          couchDBLinker.m_host = dbHost;
        }

      }
    },
    couchdb_port {
      @Override
      void fire(CouchDBLinker couchDBLinker, ConfigurationProperty prop) {
        String dbPort = couchDBLinker.getCfgValue(prop);
        if (null != dbPort) {
          try {
            couchDBLinker.m_port = Integer.parseInt(dbPort);
          } catch (Exception ex) {
          }
        }
      }
    },
    couchdb_admin {
      @Override
      void fire(CouchDBLinker couchDBLinker, ConfigurationProperty prop) {
        String dbAdmin = couchDBLinker.getCfgValue(prop);
        if (null != dbAdmin) {
          couchDBLinker.m_adminName = dbAdmin;
        }
      }
    },
    couchdb_pass {
      @Override
      void fire(CouchDBLinker couchDBLinker, ConfigurationProperty prop) {
        String dbPass = couchDBLinker.getCfgValue(prop);
        if (null != dbPass) {
          couchDBLinker.m_adminPswd = dbPass;
        }
      }
    },
    js_type {
      @Override
      void fire(CouchDBLinker couchDBLinker, ConfigurationProperty prop) {
        couchDBLinker.m_jsType = couchDBLinker.getCfgValue(prop);
      }
    },
    design_name {
      @Override
      void fire(CouchDBLinker couchDBLinker, ConfigurationProperty prop) {
        couchDBLinker.m_designName = couchDBLinker.getCfgValue(prop);
      }
    },
    name {
      @Override
      void fire(CouchDBLinker couchDBLinker, ConfigurationProperty prop) {
        couchDBLinker.m_name = couchDBLinker.getCfgValue(prop);
      }
    };

    abstract void fire(CouchDBLinker couchDBLinker, ConfigurationProperty prop);
  }

  public String m_host = "localhost";
  public int m_port = 5984;
  public String m_dbName = "xxx";
  public String m_adminName = null;
  public String m_adminPswd = null;

  public String m_designName = "dn";
  public String m_name = "vn";
  public String m_jsType = null;

  public CouchDBLinker() throws NoSuchAlgorithmException {
  }

  @Override
  public String getDescription() {
    return "CouchDB App linker";
  }

  public String getCfgValue(final ConfigurationProperty pProp) {
    /**
     * List<String> vals = pProp.getValues(); if(vals == null || vals.isEmpty())
     * { return null; } return vals.get(0);
     */
    return pProp.getValue();
  }

  private void setupParams(final LinkerContext pContext) {
    SortedSet<ConfigurationProperty> props = pContext.getConfigurationProperties();
    for (ConfigurationProperty prop : props) {
      SetupMeta.valueOf(prop.getName()).fire(this, prop);
    }
  }

  @Override
  public ArtifactSet link(final TreeLogger pLogger, final LinkerContext pContext, final ArtifactSet pArtifacts)
      throws UnableToCompleteException {
    ArtifactSet toReturn = new ArtifactSet(pArtifacts);
    SortedSet<EmittedArtifact> emitted = toReturn.find(EmittedArtifact.class);
    setupParams(pContext);
    uploadScript(pLogger, pContext, emitted);
    return toReturn;
  }

  private void uploadScript(TreeLogger pLogger, final LinkerContext pContext, final SortedSet<EmittedArtifact> pArtifacts)
      throws UnableToCompleteException {
    pLogger = pLogger.branch(TreeLogger.DEBUG, "uploadScript", null);
    Database db = new Database(m_host, m_port, m_dbName);
    if (null != m_adminName) {
      Map<String, String> pm = new HashMap<String, String>();
      pm.put("name", m_adminName);
      pm.put("password", m_adminPswd);
      Response resp = db.getServer().post("/_session", pm);
      if (200 != resp.getCode()) {
        pLogger.log(TreeLogger.WARN, MessageFormat.format("Cannot authenticate :{0}:{1}", resp.getCode(), resp.getContentAsString()));
      }
    }
    DesignDocument dd;
    try {
      dd = db.getDesignDocument(m_designName);
    } catch (NotFoundException nex) {
      dd = new DesignDocument(m_designName);
      db.createDocument(dd);
    }
    handleEmittedArtifact(pLogger, pArtifacts, db);
  }

  private void handleEmittedArtifact(final TreeLogger pLogger, final SortedSet<EmittedArtifact> pArtifacts, final Database db) throws UnableToCompleteException {
    DesignDocument dd;
    for (EmittedArtifact artifact : pArtifacts) {
      if (!artifact.isPrivate()) {
        String path = artifact.getPartialPath();
        pLogger.log(TreeLogger.DEBUG, "artifact path : " + path, null);
        if (path.endsWith(".js")) {
          InputStream in = artifact.getContents(pLogger);
          byte[] buffer = new byte[4096];
          int read;
          ByteArrayOutputStream baos = new ByteArrayOutputStream(8192);
          try {
            while (-1 != (read = in.read(buffer))) {
              baos.write(buffer, 0, read);
            }
            in.close();
            String script = new String(baos.toByteArray());
            pLogger.log(TreeLogger.DEBUG, "upload resource = " + path + " as " + m_jsType);
            String pMimeType = "application/x-javascript";
            dd = db.getDesignDocument(m_designName);
            ResponseType responseType = ResponseType.valueOf(m_jsType);
            responseType.fire(this, m_name, db, dd, script, pLogger);

          } catch (Exception e) {
            pLogger.log(TreeLogger.ERROR, MessageFormat.format("Unable to read/upload artifact {0}", artifact.getPartialPath()), e);
            throw new UnableToCompleteException();
          }     // These artifacts won't be in the module output directory
        }
      }
    }
  }


}
