/**
 * A lazily loaded container that only fetches the template string and content when the 
 * container is first shown. Subclasses should check this._loaded before doing any template 
 * related work in the widget lifecycle events.
 */
define([
  'dojo/_base/lang',
  'dojo/_base/declare', 
  'dijit/_WidgetBase',
  'dijit/_Container',
  'dijit/_Contained',
  'dijit/_TemplatedMixin',
  'dijit/_WidgetsInTemplateMixin',
  'dojo/Deferred',
  'dojo/DeferredList',
  'dojo/dom-attr',
  'landos/LoadingPanel'
], function(lang, declare, _WidgetBase, _Container, _Contained, _TemplatedMixin, _WidgetsInTemplateMixin, Deferred, DeferredList, domAttr) {
  
  return declare([_WidgetBase, _Container, _Contained, _TemplatedMixin, _WidgetsInTemplateMixin], {
    templateString: 
       '<div data-dojo-attach-point="containerNode">'
     +   '<div data-dojo-type="landos/LoadingPanel" data-dojo-attach-point="_loading_cover"></div>'
     + '</div>',
     
    _busy: false,
    _loaded: false,
    
    _onShow: function(){
      // summary:
      //    Internal method called when this widget is made visible.
      //    See `onShow` for details.
      this.onShow();
    },
    
    // Cancel _WidgetBase's _setTitleAttr because we don't want the title attribute (used to specify
    // tab labels) to be copied to ContentPane.domNode... otherwise a tooltip shows up over the
    // entire pane.
    _setTitleAttr: null,
    
    /**
     * Implementors should override this function, providing an implementation to fetch their real template.
     * @returns A Deferred that will contain the templateString to use.
     */
    getRealTemplateString: function() {
      var deferred = new Deferred();
      deferred.resolve(template);
      return deferred.promise;
    },
   
    onShow: function() {
      if (!this._loaded) {
        var old = this.domNode,
            parent = old.parentNode,
            sibling = old.previousSibling,
            loading = new Deferred();

        this.busy(loading);
        this.getRealTemplateString().then(lang.hitch(this, function(template) {
          var clsstr = domAttr.get(this.domNode, 'class');
          this.destroyRendering();
          this.destroy();
          delete this._destroyed;
          delete this._started;
          this._loaded = true;
          
          this.templateString = template;
          this.create();
          domAttr.set(this.domNode, 'class', clsstr);
          
          if (sibling)
            this.placeAt(sibling, 'after');
          else
            this.placeAt(parent);
          
          loading.resolve();
        })).otherwise(lang.hitch(this, function(error) {
          loading.reject(error);
          console.error(error);
        }));
      }
    },
    
    busy: function(deferred) {
      if (deferred) {
        if (this._busy && !this._busy.isFulfilled()) {
          var oldd = this._busy,
              newd = this._busy = new Deferred();
          oldd.then(function(v) { newd.resolve(v); }).otherwise(function(e) { newd.reject(e); });
        } else {
          this._busy = deferred;
        }
        this._loading_cover.show();
        this._busy.then(lang.hitch(this, this._isLoaded)).otherwise(lang.hitch(this, this._isLoaded));
      }
      return this._busy;
    }, 
    
    _isLoaded: function() {
      if (this.busy().isResolved())
        this._loading_cover.hide();
    }
  });
  
});