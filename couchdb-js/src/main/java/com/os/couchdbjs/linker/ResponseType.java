package com.os.couchdbjs.linker;

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.TypeElement;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.os.couchdbjs.client.annotations.FilterFn;
import com.os.couchdbjs.client.annotations.FulltextFn;
import com.os.couchdbjs.client.annotations.ListFn;
import com.os.couchdbjs.client.annotations.ShowFn;
import com.os.couchdbjs.client.annotations.UpdateHandlerFn;
import com.os.couchdbjs.client.annotations.ValidateDocUpdateFn;
import com.os.couchdbjs.client.annotations.ViewMapFn;
import com.os.couchdbjs.client.annotations.ViewReduceFn;
import com.os.couchdbjs.processor.Processor;
import org.jcouchdb.db.Database;
import org.jcouchdb.document.DesignDocument;
import org.jcouchdb.document.View;

/**
 * User: jim
 * Date: 10/13/11
 * Time: 11:40 PM
 */
public enum ResponseType {
  map(ViewMapFn.class, "function(doc) {", "global_gwt.map(doc);") {
    @Override
    void processTypeElement(TypeElement pElement, String[] a, AnnotationValue designDocName) {
      if (designDocName != null) {
        a[0] = designDocName.toString();
      }
      AnnotationValue viewName = Processor.getAnnotationValue(Processor.getAnnotationMirror(pElement, ViewMapFn.class), "viewName");
      if (viewName != null) {
        a[1] = viewName.toString();
      }

    }

    @Override
    void fire(CouchDBLinker couchDBLinker, String m_name, Database db, DesignDocument dd, String script, TreeLogger pLogger) throws UnableToCompleteException {
      View view = dd.getView(m_name);
      if (view == null) view = new View(script);
      else view.setMap(script);
      dd.getViews().put(m_name, view);
      db.createOrUpdateDocument(dd);
    }

    @Override
    public String ppath(String m_designName, String m_name) {
      return m_designName + "/views/" + m_name + "/map.js";
    }
  },
  reduce(ViewReduceFn.class, "function(keys,values) {", "return global_gwt.reduce(keys,values);") {
    @Override
    public String ppath(String m_designName, String m_name) {
      return m_designName + "/views/" + m_name + "/reduce.js";
    }

    @Override
    void processTypeElement(TypeElement pElement, String[] a, AnnotationValue designDocName) {
              if (designDocName != null) {
          a[0] = Processor.getAnnotationValue(Processor.getAnnotationMirror(pElement, ViewReduceFn.class), "designDocName").toString();
        }
        AnnotationValue viewName = Processor.getAnnotationValue(Processor.getAnnotationMirror(pElement, ViewReduceFn.class), "viewName");
        if (viewName != null) {
          a[1] = viewName.toString();
        }
    }

    @Override
    void fire(CouchDBLinker couchDBLinker, String m_name, Database db, DesignDocument dd, String script, TreeLogger pLogger) throws UnableToCompleteException {
      View view = dd.getView(m_name);
      if (null == view) {
        pLogger.log(TreeLogger.Type.ERROR, "Cannot have reduce without map for " + m_name);
        throw new UnableToCompleteException();
      } else view.setReduce(script);
      dd.getViews().put(m_name, view);
      db.createOrUpdateDocument(dd);
    }
  },
  show(ShowFn.class, "function(doc,req) {", "return global_gwt.show(doc,req);") {
    @Override
    void processTypeElement(TypeElement pElement, String[] a, AnnotationValue designDocName) {
      if (designDocName != null) {
          a[0] = designDocName.toString();
        }
        AnnotationValue showName = Processor.getAnnotationValue(Processor.getAnnotationMirror(pElement, ShowFn.class), "showName");
        if (showName != null) {
          a[1] = showName.toString();
        } //todo: verify for a purpose
    }

    @Override
    public String ppath(String m_designName, String m_name) {
      return m_designName + "/shows/" + m_name + ".js";
    }

    @Override
    void fire(CouchDBLinker couchDBLinker, String m_name, Database db, DesignDocument dd, String script, TreeLogger pLogger) {
      dd.addShowFunction(m_name, script);
      db.createOrUpdateDocument(dd);
    }
  },
  list(ListFn.class, "function(head,req) {", "return global_gwt.list(head,req);") {
    @Override
    void processTypeElement(TypeElement pElement, String[] a, AnnotationValue designDocName) {
             if (designDocName != null) {
          a[0] = designDocName.toString();
        }
        AnnotationValue listName = Processor.getAnnotationValue(Processor.getAnnotationMirror(pElement, ListFn.class), "listName");
        if (listName != null) {
          a[1] = listName.toString();
        }
    }

    @Override
    public String ppath(String m_designName, String m_name) {
      return m_designName + "/lists/" + m_name + ".js";
    }

    @Override
    void fire(CouchDBLinker couchDBLinker, String m_name, Database db, DesignDocument dd, String script, TreeLogger pLogger) {
      dd.addListFunction(m_name, script);
      db.createOrUpdateDocument(dd);
    }
  },
  filter(FilterFn.class, "function(doc,req) {", "return global_gwt.filter(doc,req);") {
    @Override
    public String ppath(String m_designName, String m_name) {
      return m_designName + "/filters/" + m_name + ".js";
    }

    @Override
    void processTypeElement(TypeElement pElement, String[] a, AnnotationValue designDocName) {
                if (designDocName != null) {
          a[0] = designDocName.toString();
        }
        AnnotationValue filterName = Processor.getAnnotationValue(Processor.getAnnotationMirror(pElement, FilterFn.class), "filterName");
        if (filterName != null) {
          a[1] = filterName.toString();
        }
    }

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
  validate(ValidateDocUpdateFn.class, "function(newDoc,oldDoc,userCtx) {", "global_gwt.validate(newDoc,oldDoc,userCtx);") {
    @Override
    void processTypeElement(TypeElement pElement, String[] a, AnnotationValue designDocName) {
       if (designDocName != null) {
          a[0] = designDocName.toString();
        }
    }

    @Override
    public String ppath(String m_designName, String m_name) {
      return m_name + "/validat_doc_update.js";
    }

    @Override
    void fire(CouchDBLinker couchDBLinker, String m_name, Database db, DesignDocument dd, String script, TreeLogger pLogger) {
      dd.setValidateOnDocUpdate(script);
      db.createOrUpdateDocument(dd);
    }
  },
  fulltext(FulltextFn.class, "function(doc) {", "return global_gwt.fulltext(doc);") {
    @Override
    void processTypeElement(TypeElement pElement, String[] a, AnnotationValue designDocName) {
             if (designDocName != null) {
          a[0] = designDocName.toString();
        }
        AnnotationValue idxName = Processor.getAnnotationValue(Processor.getAnnotationMirror(pElement, FulltextFn.class), "idxName");
        if (idxName != null) {
          a[1] = idxName.toString();
        }
    }

    @Override
    public String ppath(String m_designName, String m_name) {
      return m_designName + "/fulltext/" + m_name + ".js";
    }

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
  update(UpdateHandlerFn.class, "function(doc,req) {", "return global_gwt.update(doc,req);") {
    @Override
    public String ppath(String m_designName, String m_name) {
      return m_designName + "/update/" + m_name + ".js";
    }

    @Override
    void processTypeElement(TypeElement pElement, String[] a, AnnotationValue designDocName) {      if (designDocName != null) {
          a[0] = designDocName.toString();
        }
        AnnotationValue av = Processor.getAnnotationValue(Processor.getAnnotationMirror(pElement, UpdateHandlerFn.class), "name");
        if (av != null) {
          a[1] = av.toString();
        }
      //todo: verify for a purpose
    }

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
  public Class<? extends Annotation> annotationClass;
  String stopen;
  String stclose;


  ResponseType(Type annotationClass, String stopen, String
      stclose) {
    this.annotationClass = (Class<? extends Annotation>) annotationClass;
    //To change body of created methods use File | Settings | File Templates.
    this.stopen = stopen;
    this.stclose = stclose;
  }

  /**
   *             util method to assign the js peers
   *
   * @param processor
   * @param pElement
   * @param pJsType
   * @return docname, name pair</>
   */
  public  static String[]processAnnotatedValue(Processor processor, TypeElement pElement, String pJsType) {
    String a[]=new String[2];
    ResponseType responseType = valueOf(pJsType);
    Class<? extends Annotation> annotatedElement = responseType.annotationClass;
    final AnnotationValue designDocName = Processor.getAnnotationValue(Processor.getAnnotationMirror(pElement, (Class<? extends Annotation>) annotatedElement), "designDocName");
    Processor x = processor;
    responseType.processTypeElement(pElement, a,designDocName);
    return a;  //todo: review control flow


  }

  abstract void processTypeElement(TypeElement pElement, String[] a, AnnotationValue designDocName);


  abstract void fire(CouchDBLinker couchDBLinker, String m_name, Database db, DesignDocument dd, String script, TreeLogger pLogger) throws UnableToCompleteException;


  public abstract String ppath(String m_designName, String m_name);
}
