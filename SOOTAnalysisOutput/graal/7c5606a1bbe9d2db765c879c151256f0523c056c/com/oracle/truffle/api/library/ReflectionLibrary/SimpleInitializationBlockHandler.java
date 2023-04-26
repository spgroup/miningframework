package com.oracle.truffle.api.library;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.locks.Lock;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.GeneratedBy;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.library.GenerateLibrary.Abstract;
import com.oracle.truffle.api.library.GenerateLibrary.DefaultExport;
import com.oracle.truffle.api.library.ReflectionLibraryDefault.Send;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeCost;
import com.oracle.truffle.api.nodes.NodeUtil;

@GenerateLibrary
@DefaultExport(ReflectionLibraryDefault.class)
public abstract class ReflectionLibrary extends Library {

    protected ReflectionLibrary() {
    }

    @Abstract
    public abstract Object send(Object receiver, Message message, Object... args) throws Exception;

    private static final LibraryFactory<ReflectionLibrary> FACTORY = LibraryFactory.resolve(ReflectionLibrary.class);

    public static LibraryFactory<ReflectionLibrary> getFactory() {
        return FACTORY;
    }
}

@ExportLibrary(value = ReflectionLibrary.class, receiverType = Object.class)
final class ReflectionLibraryDefault {

    static final int LIMIT = 8;

    @ExportMessage
    static class Send {

        @Specialization(guards = { "message == cachedMessage", "cachedLibrary.accepts(receiver)" }, limit = "LIMIT")
        static Object doSendCached(Object receiver, Message message, Object[] args, @Cached("message") Message cachedMessage, @Cached("message.getFactory().create(receiver)") Library cachedLibrary) throws Exception {
            return message.getFactory().genericDispatch(cachedLibrary, receiver, cachedMessage, args, 0);
        }

        @Specialization(replaces = "doSendCached")
        @TruffleBoundary
        static Object doSendGeneric(Object receiver, Message message, Object[] args) throws Exception {
            LibraryFactory<?> lib = message.getFactory();
            return lib.genericDispatch(lib.getUncached(receiver), receiver, message, args, 0);
        }
    }
}

@GeneratedBy(ReflectionLibraryDefault.class)
final class ReflectionLibraryDefaultGen {

    private static final LibraryFactory<DynamicDispatchLibrary> DYNAMIC_DISPATCH_LIBRARY_ = LibraryFactory.resolve(DynamicDispatchLibrary.class);

    static {
        LibraryExport.register(ReflectionLibraryDefault.class, new ReflectionLibraryExports());
    }

    private ReflectionLibraryDefaultGen() {
    }

    @GeneratedBy(ReflectionLibraryDefault.class)
    private static final class ReflectionLibraryExports extends LibraryExport<ReflectionLibrary> {

        private ReflectionLibraryExports() {
            super(ReflectionLibrary.class, Object.class, true);
        }

        @Override
        protected ReflectionLibrary createUncached(Object receiver) {
            return new Uncached(receiver);
        }

        @Override
        protected ReflectionLibrary createCached(Object receiver) {
            return new Cached(receiver);
        }

        @GeneratedBy(ReflectionLibraryDefault.class)
        private static final class Cached extends ReflectionLibrary {

            @Child
            private DynamicDispatchLibrary dynamicDispatch_;

            private final Class<?> dynamicDispatchTarget_;

            @CompilationFinal
            private int state_;

            @CompilationFinal
            private int exclude_;

            @Child
            private SendCachedData sendCached_cache;

            Cached(Object receiver) {
                this.dynamicDispatch_ = DYNAMIC_DISPATCH_LIBRARY_.create(receiver);
                this.dynamicDispatchTarget_ = DYNAMIC_DISPATCH_LIBRARY_.getUncached(receiver).dispatch(receiver);
            }

            @Override
            public boolean accepts(Object receiver) {
                return dynamicDispatch_.accepts(receiver) && dynamicDispatch_.dispatch(receiver) == dynamicDispatchTarget_;
            }

            @ExplodeLoop
            @Override
            public Object send(Object arg0Value, Message arg1Value, Object... arg2Value) throws Exception {
                assert getRootNode() != null : "Invalid libray usage. Cached library must be adopted by a RootNode before it is executed.";
                int state = state_;
                if (state != 0) {
                    if ((state & 0b1) != 0) {
                        SendCachedData s1_ = this.sendCached_cache;
                        while (s1_ != null) {
                            if ((arg1Value == s1_.cachedMessage_) && (s1_.cachedLibrary_.accepts(arg0Value))) {
                                return Send.doSendCached(arg0Value, arg1Value, arg2Value, s1_.cachedMessage_, s1_.cachedLibrary_);
                            }
                            s1_ = s1_.next_;
                        }
                    }
                    if ((state & 0b10) != 0) {
                        return Send.doSendGeneric(arg0Value, arg1Value, arg2Value);
                    }
                }
                CompilerDirectives.transferToInterpreterAndInvalidate();
                return executeAndSpecialize(arg0Value, arg1Value, arg2Value);
            }

            private Object executeAndSpecialize(Object arg0Value, Message arg1Value, Object[] arg2Value) throws Exception {
                Lock lock = getLock();
                boolean hasLock = true;
                lock.lock();
                int state = state_;
                int exclude = exclude_;
                try {
                    if ((exclude) == 0) {
                        int count1_ = 0;
                        SendCachedData s1_ = this.sendCached_cache;
                        if ((state & 0b1) != 0) {
                            while (s1_ != null) {
                                if ((arg1Value == s1_.cachedMessage_) && (s1_.cachedLibrary_.accepts(arg0Value))) {
                                    break;
                                }
                                s1_ = s1_.next_;
                                count1_++;
                            }
                        }
                        if (s1_ == null) {
                            Library cachedLibrary__ = super.insert((arg1Value.getFactory().create(arg0Value)));
                            if ((cachedLibrary__.accepts(arg0Value)) && count1_ < (ReflectionLibraryDefault.LIMIT)) {
                                s1_ = super.insert(new SendCachedData(sendCached_cache));
                                s1_.cachedMessage_ = (arg1Value);
                                s1_.cachedLibrary_ = s1_.insertAccessor(cachedLibrary__);
                                this.sendCached_cache = s1_;
                                this.state_ = state = state | 0b1;
                            }
                        }
                        if (s1_ != null) {
                            lock.unlock();
                            hasLock = false;
                            return Send.doSendCached(arg0Value, arg1Value, arg2Value, s1_.cachedMessage_, s1_.cachedLibrary_);
                        }
                    }
                    this.exclude_ = exclude = exclude | 0b1;
                    this.sendCached_cache = null;
                    state = state & 0xfffffffe;
                    this.state_ = state = state | 0b10;
                    lock.unlock();
                    hasLock = false;
                    return Send.doSendGeneric(arg0Value, arg1Value, arg2Value);
                } finally {
                    if (hasLock) {
                        lock.unlock();
                    }
                }
            }

            @Override
            public NodeCost getCost() {
                int state = state_;
                if (state == 0b0) {
                    return NodeCost.UNINITIALIZED;
                } else if ((state & (state - 1)) == 0) {
                    SendCachedData s1_ = this.sendCached_cache;
                    if ((s1_ == null || s1_.next_ == null)) {
                        return NodeCost.MONOMORPHIC;
                    }
                }
                return NodeCost.POLYMORPHIC;
            }

            @GeneratedBy(ReflectionLibraryDefault.class)
            private static final class SendCachedData extends Node {

                @Child
                SendCachedData next_;

                @CompilationFinal
                Message cachedMessage_;

                @Child
                Library cachedLibrary_;

                SendCachedData(SendCachedData next_) {
                    this.next_ = next_;
                }

                @Override
                public NodeCost getCost() {
                    return NodeCost.NONE;
                }

                <T extends Node> T insertAccessor(T node) {
                    return super.insert(node);
                }
            }
        }

        @GeneratedBy(ReflectionLibraryDefault.class)
        private static final class Uncached extends ReflectionLibrary {

            @Child
            private DynamicDispatchLibrary dynamicDispatch_;

            private final Class<?> dynamicDispatchTarget_;

            Uncached(Object receiver) {
                this.dynamicDispatch_ = DYNAMIC_DISPATCH_LIBRARY_.getUncached(receiver);
                this.dynamicDispatchTarget_ = dynamicDispatch_.dispatch(receiver);
            }

            @Override
            public boolean accepts(Object receiver) {
                return dynamicDispatch_.accepts(receiver) && dynamicDispatch_.dispatch(receiver) == dynamicDispatchTarget_;
            }

            @Override
            public boolean isAdoptable() {
                return false;
            }

            @Override
            public NodeCost getCost() {
                return NodeCost.MEGAMORPHIC;
            }

            @TruffleBoundary
            @Override
            public Object send(Object arg0Value, Message arg1Value, Object... arg2Value) throws Exception {
                return Send.doSendGeneric(arg0Value, arg1Value, arg2Value);
            }
        }
    }
}

@GeneratedBy(ReflectionLibrary.class)
final class ReflectionLibraryGen extends LibraryFactory<ReflectionLibrary> {

    private static final Class<ReflectionLibrary> LIBRARY_CLASS = ReflectionLibraryGen.lazyLibraryClass();

    private static final Message SEND = new MessageImpl("send", 0, Object.class, Object.class, Message.class, Object[].class);

    private static final ReflectionLibraryGen INSTANCE = new ReflectionLibraryGen();

    static {
        LibraryFactory.register(ReflectionLibraryGen.LIBRARY_CLASS, INSTANCE);
    }

    private ReflectionLibraryGen() {
        super(ReflectionLibraryGen.LIBRARY_CLASS, Collections.unmodifiableList(Arrays.asList(ReflectionLibraryGen.SEND)));
    }

    @Override
    protected Class<?> getDefaultClass(Object receiver) {
        return ReflectionLibraryDefault.class;
    }

    @Override
    protected ReflectionLibrary createProxy(ReflectionLibrary library) {
        return new Proxy(library);
    }

    @Override
    protected Object genericDispatch(Library originalLib, Object receiver, Message message, Object[] args, int offset) throws Exception {
        ReflectionLibrary lib = (ReflectionLibrary) originalLib;
        MessageImpl messageImpl = (MessageImpl) message;
        if (messageImpl.getParameterCount() - 1 != args.length - offset) {
            CompilerDirectives.transferToInterpreter();
            throw new IllegalArgumentException("Invalid number of arguments.");
        }
        switch(messageImpl.index) {
            case 0:
                return lib.send(receiver, (Message) args[offset], (Object[]) args[offset + 1]);
        }
        CompilerDirectives.transferToInterpreter();
        throw new AbstractMethodError(message.toString());
    }

    @Override
    protected ReflectionLibrary createDispatchImpl(int limit) {
        return new CachedDispatchFirst(null, null, limit);
    }

    @Override
    protected ReflectionLibrary createUncachedDispatch() {
        return new UncachedDispatch();
    }

    @SuppressWarnings("unchecked")
    private static Class<ReflectionLibrary> lazyLibraryClass() {
        try {
            return (Class<ReflectionLibrary>) Class.forName("com.oracle.truffle.api.library.ReflectionLibrary", false, ReflectionLibraryGen.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new AssertionError(e);
        }
    }

    @GeneratedBy(ReflectionLibrary.class)
    private static class MessageImpl extends Message {

        final int index;

        MessageImpl(String name, int index, Class<?> returnType, Class<?>... parameters) {
            super(ReflectionLibraryGen.LIBRARY_CLASS, name, returnType, parameters);
            this.index = index;
        }
    }

    @GeneratedBy(ReflectionLibrary.class)
    private static final class Proxy extends ReflectionLibrary {

        @Child
        private ReflectionLibrary lib;

        Proxy(ReflectionLibrary lib) {
            this.lib = lib;
        }

        @Override
        public Object send(Object receiver_, Message message, Object... args) throws Exception {
            try {
                return lib.send(receiver_, ReflectionLibraryGen.SEND, message, args);
            } catch (Exception e_) {
                throw e_;
            }
        }

        @Override
        public boolean accepts(Object receiver_) {
            return lib.accepts(receiver_);
        }
    }

    @GeneratedBy(ReflectionLibrary.class)
    private static final class CachedToUncachedDispatch extends ReflectionLibrary {

        @Override
        public NodeCost getCost() {
            return NodeCost.MEGAMORPHIC;
        }

        @TruffleBoundary
        @Override
        public Object send(Object receiver_, Message message, Object... args) throws Exception {
            assert getRootNode() != null : "Invalid libray usage. Cached library must be adopted by a RootNode before it is executed.";
            Node prev_ = NodeUtil.pushEncapsulatingNode(getParent());
            try {
                return INSTANCE.getUncached(receiver_).send(receiver_, message, args);
            } finally {
                NodeUtil.popEncapsulatingNode(prev_);
            }
        }

        @Override
        public boolean accepts(Object receiver_) {
            return true;
        }
    }

    @GeneratedBy(ReflectionLibrary.class)
    private static final class UncachedDispatch extends ReflectionLibrary {

        @Override
        public NodeCost getCost() {
            return NodeCost.MEGAMORPHIC;
        }

        @TruffleBoundary
        @Override
        public Object send(Object receiver_, Message message, Object... args) throws Exception {
            return INSTANCE.getUncached(receiver_).send(receiver_, message, args);
        }

        @TruffleBoundary
        @Override
        public boolean accepts(Object receiver_) {
            return true;
        }

        @Override
        public boolean isAdoptable() {
            return false;
        }
    }

    @GeneratedBy(ReflectionLibrary.class)
    private static final class CachedDispatchNext extends CachedDispatch {

        CachedDispatchNext(ReflectionLibrary library, CachedDispatch next) {
            super(library, next);
        }

        @Override
        int getLimit() {
            throw new AssertionError();
        }

        @Override
        public NodeCost getCost() {
            return NodeCost.NONE;
        }
    }

    @GeneratedBy(ReflectionLibrary.class)
    private static final class CachedDispatchFirst extends CachedDispatch {

        private final int limit_;

        CachedDispatchFirst(ReflectionLibrary library, CachedDispatch next, int limit_) {
            super(library, next);
            this.limit_ = limit_;
        }

        @Override
        int getLimit() {
            return this.limit_;
        }

        @Override
        public NodeCost getCost() {
            if (this.library instanceof CachedToUncachedDispatch) {
                return NodeCost.MEGAMORPHIC;
            }
            CachedDispatch current = this;
            int count = 0;
            do {
                if (current.library != null) {
                    count++;
                }
                current = current.next;
            } while (current != null);
            return NodeCost.fromCount(count);
        }
    }

    @GeneratedBy(ReflectionLibrary.class)
    private abstract static class CachedDispatch extends ReflectionLibrary {

        @Child
        ReflectionLibrary library;

        @Child
        CachedDispatch next;

        CachedDispatch(ReflectionLibrary library, CachedDispatch next) {
            this.library = library;
            this.next = next;
        }

        abstract int getLimit();

        @ExplodeLoop
        @Override
        public Object send(Object receiver_, Message message, Object... args) throws Exception {
            do {
                CachedDispatch current = this;
                do {
                    ReflectionLibrary thisLibrary = current.library;
                    if (thisLibrary != null && thisLibrary.accepts(receiver_)) {
                        return thisLibrary.send(receiver_, message, args);
                    }
                    current = current.next;
                } while (current != null);
                CompilerDirectives.transferToInterpreterAndInvalidate();
                specialize(receiver_);
            } while (true);
        }

        @Override
        public boolean accepts(Object receiver_) {
            return true;
        }

        private void specialize(Object receiver_) {
            CachedDispatch current = this;
            ReflectionLibrary thisLibrary = current.library;
            if (thisLibrary == null) {
                this.library = insert(INSTANCE.create(receiver_));
            } else {
                Lock lock = getLock();
                lock.lock();
                try {
                    int count = 0;
                    do {
                        ReflectionLibrary currentLibrary = current.library;
                        if (currentLibrary != null && currentLibrary.accepts(receiver_)) {
                            return;
                        }
                        count++;
                        current = current.next;
                    } while (current != null);
                    if (count >= getLimit()) {
                        this.library = insert(new CachedToUncachedDispatch());
                        this.next = null;
                    } else {
                        this.next = insert(new CachedDispatchNext(INSTANCE.create(receiver_), next));
                    }
                } finally {
                    lock.unlock();
                }
            }
        }
    }
}