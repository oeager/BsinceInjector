package com.developer.bsince.ioc.core;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.View;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.developer.bsince.ioc.core.BsinceProcessor.ANDROID_PREFIX;
import static com.developer.bsince.ioc.core.BsinceProcessor.JAVA_PREFIX;

/**
 * Created by oeager on 2015/5/1.
 */
public final class BsinceInject {
    private BsinceInject() {
        throw new AssertionError("No instances.");
    }

    final static Map<Class<?>,Ejector<Object>> EJECTORS = new LinkedHashMap<>();


    public enum Finder {
        VIEW {
            @Override
            protected View findView(Object source, int id) {
                return ((View) source).findViewById(id);
            }

            @Override
            public Context getContext(Object source) {
                return ((View) source).getContext();
            }
        },
        ACTIVITY {
            @Override
            protected View findView(Object source, int id) {
                return ((Activity) source).findViewById(id);
            }

            @Override
            public Context getContext(Object source) {
                return (Activity) source;
            }
        },
        DIALOG {
            @Override
            protected View findView(Object source, int id) {
                return ((Dialog) source).findViewById(id);
            }

            @Override
            public Context getContext(Object source) {
                return ((Dialog) source).getContext();
            }
        };

        private static <T> T[] filterNull(T[] views) {
            int newSize = views.length;
            for (T view : views) {
                if (view == null) {
                    newSize -= 1;
                }
            }
            if (newSize == views.length) {
                return views;
            }
            //noinspection unchecked
            @SuppressWarnings("unchecked")
			T[] newViews = (T[]) new Object[newSize];
            int nextIndex = 0;
            for (T view : views) {
                if (view != null) {
                    newViews[nextIndex++] = view;
                }
            }
            return newViews;
        }

        @SafeVarargs
		public static <T> T[] arrayOf(T... views) {
            return filterNull(views);
        }
        @SafeVarargs
		public static <T> List<T> listOf(T... views) {
            return new UnmodifiableList<T>(filterNull(views));
        }
        public <T> T findRequiredView(Object source, int id, String who) {
            T view = findOptionalView(source, id, who);
            if (view == null) {
                String name = getContext(source).getResources().getResourceEntryName(id);
                throw new IllegalStateException("Required view '"
                        + name
                        + "' with ID "
                        + id
                        + " for "
                        + who
                        + " was not found. If this view is optional add '@Nullable' annotation.");
            }
            return view;
        }

        public <T> T findOptionalView(Object source, int id, String who) {
            View view = findView(source, id);
            return castView(view, id, who);
        }

        @SuppressWarnings("unchecked") // That's the point.
        public <T> T castView(View view, int id, String who) {
            try {
                return (T) view;
            } catch (ClassCastException e) {
                if (who == null) {
                    throw new AssertionError();
                }
                String name = view.getResources().getResourceEntryName(id);
                throw new IllegalStateException("View '"
                        + name
                        + "' with ID "
                        + id
                        + " for "
                        + who
                        + " was of the wrong type. See cause for more info.", e);
            }
        }

        @SuppressWarnings("unchecked") // That's the point.
        public <T> T castParam(Object value, String from, int fromPosition, String to, int toPosition) {
            try {
                return (T) value;
            } catch (ClassCastException e) {
                throw new IllegalStateException("Parameter #"
                        + (fromPosition + 1)
                        + " of method '"
                        + from
                        + "' was of the wrong type for parameter #"
                        + (toPosition + 1)
                        + " of method '"
                        + to
                        + "'. See cause for more info.", e);
            }
        }

        protected abstract View findView(Object source, int id);

        public abstract Context getContext(Object source);
    }


    public interface Ejector<T>{
        void inject(Finder finder,T target,Object source);

        void outPour(T target);
    }

    final static Ejector<Object> NOP_VIEW_BINDER = new Ejector<Object>() {
        @Override
        public void inject(Finder finder, Object target, Object source) {

        }

        @Override
        public void outPour(Object target) {

        }
    };

    public static void inject(Activity target) {
        inject(target, target, Finder.ACTIVITY);
    }

    public static void inject(View target) {
        inject(target, target, Finder.VIEW);
    }

    public static void inject(Dialog target) {
        inject(target, target, Finder.DIALOG);
    }


    public static void inject(Object target, Activity source) {
        inject(target, source, Finder.ACTIVITY);
    }


    public static void inject(Object target, View source) {
        inject(target, source, Finder.VIEW);
    }

    public static void inject(Object target, Dialog source) {
        inject(target, source, Finder.DIALOG);
    }


    static void inject(Object target, Object source, Finder finder) {
        Class<?> targetClass = target.getClass();
        try {
            Ejector<Object> ejector = findEjectorForClass(targetClass,true);
            if (ejector != null) {
                ejector.inject(finder, target, source);
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to inject views for " + targetClass.getName(), e);
        }
    }

    public static void outPour(Object target,boolean release) {
        Class<?> targetClass = target.getClass();
        try {

            Ejector<Object> ejector = findEjectorForClass(targetClass,false);
            if (ejector != null) {
                ejector.outPour(target);
                if(release){
                	EJECTORS.remove(targetClass);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to outPour views for " + targetClass.getName(), e);
        }
    }
    
    public static void terminate(){
    	EJECTORS.clear();
    }


    @SuppressWarnings("unchecked")
	private static Ejector<Object> findEjectorForClass(Class<?> cls,boolean newInstance)
            throws IllegalAccessException, InstantiationException {
        Ejector<Object> ejector = EJECTORS.get(cls);
        if (ejector != null) {
            return ejector;
        }
        String clsName = cls.getName();
        if (clsName.startsWith(ANDROID_PREFIX) || clsName.startsWith(JAVA_PREFIX)) {
            return NOP_VIEW_BINDER;
        }
        if(newInstance){
        	try {
            	
                Class<?> ejectorClass = Class.forName(clsName + BsinceProcessor.SUFFIX);
                //noinspection unchecked
                ejector = (Ejector<Object>) ejectorClass.newInstance();
            } catch (ClassNotFoundException e) {
                ejector = findEjectorForClass(cls.getSuperclass(),newInstance);
            }
        	EJECTORS.put(cls, ejector);
        }
        
        
        return ejector;
    }
}
