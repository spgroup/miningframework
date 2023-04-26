package org.deeplearning4j.rl4j.mdp.gym;

import java.io.IOException;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.SizeTPointer;
import org.deeplearning4j.gym.StepReply;
import org.deeplearning4j.rl4j.mdp.MDP;
import org.deeplearning4j.rl4j.space.ArrayObservationSpace;
import org.deeplearning4j.rl4j.space.ActionSpace;
import org.deeplearning4j.rl4j.space.Box;
import org.deeplearning4j.rl4j.space.DiscreteSpace;
import org.deeplearning4j.rl4j.space.Encodable;
import org.deeplearning4j.rl4j.space.HighLowDiscrete;
import org.deeplearning4j.rl4j.space.ObservationSpace;
import org.bytedeco.cpython.*;
import org.bytedeco.numpy.*;
import static org.bytedeco.cpython.global.python.*;
import static org.bytedeco.numpy.global.numpy.*;

@Slf4j
public class GymEnv<O, A, AS extends ActionSpace<A>> implements MDP<O, A, AS> {

    public static final String GYM_MONITOR_DIR = "/tmp/gym-dqn";

    private static void checkPythonError() {
        if (PyErr_Occurred() != null) {
            PyErr_Print();
            throw new RuntimeException("Python error occurred");
        }
    }

    private static Pointer program;

    private static PyObject globals;

    static {
        try {
            Py_SetPath(org.bytedeco.gym.presets.gym.cachePackages());
            program = Py_DecodeLocale(GymEnv.class.getSimpleName(), null);
            Py_SetProgramName(program);
            Py_Initialize();
            PyEval_InitThreads();
            PySys_SetArgvEx(1, program, 0);
            if (_import_array() < 0) {
                PyErr_Print();
                throw new RuntimeException("numpy.core.multiarray failed to import");
            }
            globals = PyModule_GetDict(PyImport_AddModule("__main__"));
            PyEval_SaveThread();
        } catch (IOException e) {
            PyMem_RawFree(program);
            throw new RuntimeException(e);
        }
    }

    private PyObject locals;

    final protected DiscreteSpace actionSpace;

    final protected ObservationSpace<O> observationSpace;

    @Getter
    final private String envId;

    @Getter
    final private boolean render;

    @Getter
    final private boolean monitor;

    private ActionTransformer actionTransformer = null;

    private boolean done = false;

    public GymEnv(String envId, boolean render, boolean monitor) {
        this.envId = envId;
        this.render = render;
        this.monitor = monitor;
        int gstate = PyGILState_Ensure();
        try {
            locals = PyDict_New();
            Py_DecRef(PyRun_StringFlags("import gym; env = gym.make('" + envId + "')", Py_single_input, globals, locals, null));
            checkPythonError();
            if (monitor) {
                Py_DecRef(PyRun_StringFlags("env = gym.wrappers.Monitor(env, '" + GYM_MONITOR_DIR + "')", Py_single_input, globals, locals, null));
                checkPythonError();
            }
            PyObject shapeTuple = PyRun_StringFlags("env.observation_space.shape", Py_eval_input, globals, locals, null);
            int[] shape = new int[(int) PyTuple_Size(shapeTuple)];
            for (int i = 0; i < shape.length; i++) {
                shape[i] = (int) PyLong_AsLong(PyTuple_GetItem(shapeTuple, i));
            }
            observationSpace = (ObservationSpace<O>) new ArrayObservationSpace<Box>(shape);
            Py_DecRef(shapeTuple);
            PyObject n = PyRun_StringFlags("env.action_space.n", Py_eval_input, globals, locals, null);
            actionSpace = new DiscreteSpace((int) PyLong_AsLong(n));
            Py_DecRef(n);
            checkPythonError();
        } finally {
            PyGILState_Release(gstate);
        }
    }

    public GymEnv(String envId, boolean render, boolean monitor, int[] actions) {
        this(envId, render, monitor);
        actionTransformer = new ActionTransformer((HighLowDiscrete) getActionSpace(), actions);
    }

    @Override
    public ObservationSpace<O> getObservationSpace() {
        return observationSpace;
    }

    @Override
    public AS getActionSpace() {
        if (actionTransformer == null)
            return (AS) actionSpace;
        else
            return (AS) actionTransformer;
    }

    @Override
    public StepReply<O> step(A action) {
        int gstate = PyGILState_Ensure();
        try {
            if (render) {
                Py_DecRef(PyRun_StringFlags("env.render()", Py_single_input, globals, locals, null));
                checkPythonError();
            }
            Py_DecRef(PyRun_StringFlags("state, reward, done, info = env.step(" + (Integer) action + ")", Py_single_input, globals, locals, null));
            checkPythonError();
            PyArrayObject state = new PyArrayObject(PyDict_GetItemString(locals, "state"));
            DoublePointer stateData = new DoublePointer(PyArray_BYTES(state)).capacity(PyArray_Size(state));
            SizeTPointer stateDims = PyArray_DIMS(state).capacity(PyArray_NDIM(state));
            double reward = PyFloat_AsDouble(PyDict_GetItemString(locals, "reward"));
            done = PyLong_AsLong(PyDict_GetItemString(locals, "done")) != 0;
            checkPythonError();
            double[] data = new double[(int) stateData.capacity()];
            stateData.get(data);
            return new StepReply(new Box(data), reward, done, null);
        } finally {
            PyGILState_Release(gstate);
        }
    }

    @Override
    public boolean isDone() {
        return done;
    }

    @Override
    public O reset() {
        int gstate = PyGILState_Ensure();
        try {
            Py_DecRef(PyRun_StringFlags("state = env.reset()", Py_single_input, globals, locals, null));
            checkPythonError();
            PyArrayObject state = new PyArrayObject(PyDict_GetItemString(locals, "state"));
            DoublePointer stateData = new DoublePointer(PyArray_BYTES(state)).capacity(PyArray_Size(state));
            SizeTPointer stateDims = PyArray_DIMS(state).capacity(PyArray_NDIM(state));
            checkPythonError();
            done = false;
            double[] data = new double[(int) stateData.capacity()];
            stateData.get(data);
            return (O) new Box(data);
        } finally {
            PyGILState_Release(gstate);
        }
    }

    @Override
    public void close() {
        int gstate = PyGILState_Ensure();
        try {
            Py_DecRef(PyRun_StringFlags("env.close()", Py_single_input, globals, locals, null));
            checkPythonError();
            Py_DecRef(locals);
        } finally {
            PyGILState_Release(gstate);
        }
    }

    @Override
    public GymEnv<O, A, AS> newInstance() {
        return new GymEnv<O, A, AS>(envId, render, monitor);
    }
}
