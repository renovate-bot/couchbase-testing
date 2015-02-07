public class HelloWorld{
    public static interface IDeviceProperty<T>{
        public Class<T> getType();

    }

    public abstract static class DoubleProperty implements IDeviceProperty<Double>{
        final public Class<Double> getType(){return Double.class;}
    }

    public abstract static class IntProperty implements IDeviceProperty<Integer>{
        final public Class<Integer> getType(){return Integer.class;}
    }

    public abstract static class StringProperty implements IDeviceProperty<String>{
        final public Class<String> getType(){return String.class;}
    }

    public abstract static class BooleanProperty implements IDeviceProperty<Boolean>{
        final public Class<Boolean> getType(){return Boolean.class;}
    }
    public static class HAS_4G extends BooleanProperty{}
    public static class DAS extends DoubleProperty{}
    public static class NFC extends BooleanProperty{}
    public static class SCREEN_SIZE extends DoubleProperty{}
    public static class OS extends StringProperty{}
    
    
    
    
    /*    {
        HAS_4G(Boolean.class),
        DAS(Double.class),
        NFC(Boolean.class),
        SCREEN_SIZE(Double.class),
        OS(String.class);
        
        Class _type;
        DeviceProperty(Class type){
            _type=type;
        }
        
        public Class getType(){
            return _type;
        }
    }*/


    public static interface IPredicatePropertyDef<T>{
        public IDeviceProperty<T> getProperty();
        public T value();
        public T minValue();
        public T maxValue();


        static class Internal<T> implements IPredicatePropertyDef<T>{
            private IDeviceProperty<T> _prop;
            private T _value;
            private T _minValue;
            private T _maxValue;

            private Internal(Builder<T> builder){
                _prop = builder.getProp();
                _value = builder.getValue();
                _minValue = builder.getMinValue();
                _maxValue = builder.getMaxValue();
            }

            public IDeviceProperty<T> getProperty(){return _prop;}
            public T value(){return _value;}
            public T minValue(){return _minValue;}
            public T maxValue(){return _maxValue;}
        }


        public static class Builder<T>{

            public  static <T> Builder<T> builder(Class<? extends IDeviceProperty<T>> clazzProp){
                return (Builder<T>)new Builder(clazzProp);
            }

            private IDeviceProperty<T> _prop;
            private T _value;
            private T _minValue;
            private T _maxValue;

            public Builder(Class<? extends IDeviceProperty<T>> clazzProp){
                try {
                    _prop = clazzProp.newInstance();
                }
                catch(Exception e){
                    throw new RuntimeException(e);
                }
            }

            public Builder<T> withValue(T value){
                _value = value;
                return this;
            }

            public Builder<T> withMinValue(T minValue){
                _minValue = minValue;
                return this;
            }

            public Builder<T> withMaxValue(T maxValue){
                _maxValue = maxValue;
                return this;
            }

            public IDeviceProperty<T> getProp(){return _prop;}
            public T getValue(){return _value;}
            public T getMinValue(){return _minValue;}
            public T getMaxValue(){return _maxValue;}

            public IPredicatePropertyDef<T> build(){ return new Internal(this);}
        }

    }



    public static void main(String []args){

        IPredicatePropertyDef<Double> predicate = IPredicatePropertyDef.Builder.builder(SCREEN_SIZE.class).withMinValue(10.0).build();

        System.out.println(String.format("Hello World %f",predicate.minValue()));
    }
}
