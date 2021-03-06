package net.workingdeveloper.java.spring.statemachine.dumper.mdt_uml2.impl.w3m;

import net.workingdeveloper.java.spring.statemachine.dumper.mdt_uml2.IId;
import net.workingdeveloper.java.spring.statemachine.dumper.mdt_uml2.IMdtUml2Model;
import net.workingdeveloper.java.spring.statemachine.dumper.mdt_uml2.Sha1Id;
import net.workingdeveloper.java.spring.statemachine.dumper.mdt_uml2.UuidId;
import org.springframework.statemachine.transition.TransitionKind;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

/**
 * Created by Christoph Graupner on 8/22/16.
 *
 * @author Christoph Graupner <christoph.graupner@workingdeveloper.net>
 */
public class MdtUml2Model implements IMdtUml2Model {
    class MUAction extends MUNode<ModelUml.MXUNode> implements IMUAction {

        private final MUNode<?> fParent;

        MUAction(ModelUml.MXUNode aUmlState, String aName, MUNode<?> aParentState) {
            super(aUmlState);
            fParent = aParentState;
        }
    }

    class MUActionEntry extends MUAction {

        MUActionEntry(ModelUml.MXUNode aUmlState, String aName, MUState<?, ?> aParentState) {
            super(aUmlState, aName, aParentState);
        }
    }

    class MUActionExit extends MUAction {

        MUActionExit(ModelUml.MXUNode aUmlState, String aName, MUState<?, ?> aParentState) {
            super(aUmlState, aName, aParentState);
        }
    }

    class MUGuard extends MUNode<ModelUml.MXUGuard> implements IMUGuard {

        MUGuard(ModelUml.MXUGuard aUmlState) {
            super(aUmlState);
        }

        @Override
        public IMUGuard addBody(String aBody) {
            fUmlState.addBody(aBody);
            return this;
        }

        @Override
        public IMUGuard addLanguage(String aLanguageName) {
            fUmlState.addLanguage(aLanguageName);
            return this;
        }
    }

    abstract class MUNode<UMLMODEL extends ModelUml.MXUNode> implements IMUNode {

        UMLMODEL fUmlState;

        MUNode(UMLMODEL aUmlState) {
            fUmlState = aUmlState;
        }

        @Override
        public <T extends IMUNode> T addComment(String aS) {
            fUmlState.addComment(aS);
            return (T) this;
        }

        public IId getId() {
            return fUmlState.getXmiId();
        }

        public String getName() {
            return fUmlState.getName();
        }

        @Override
        public <T extends IMUNode> T setName(String aName) {
            fUmlState.setName(aName);
            return (T) this;
        }
    }

    class MUPseudoState extends MUState<ModelUml.MXUStateBase, MURegionMachineSharedState>
            implements IMUPseudoState, IMUPChoiceState, IMUPDeppHistoryState, IMUPEntryPointState,
            IMUPExitPointState, IMUPForkState, IMUPInitialState, IMUPJoinState,
            IMUPJunctionState, IMUPShallowHistoryState {
        PseudoKind fKind;

        public MUPseudoState(ModelUml.MXUStateBase aUmlMXUNode, MURegionMachineSharedState aParent) {
            super(aUmlMXUNode, aParent);
        }

        @Override
        public IMUPseudoState setKind(PseudoKind aKind) {
            fKind = aKind;
            return this;
        }
    }

    abstract class MURegionMachineSharedState<UMLMODEL extends ModelUml.MXURegionMachineShared, PARENT extends MUState> extends MUState<UMLMODEL, PARENT> implements IMURegionState {

        public MURegionMachineSharedState(UMLMODEL aUmlState, PARENT aParent) {
            super(aUmlState, aParent);
        }

        @Override
        public IMUPseudoState addPseudoState(IId aUUID, String aName, UmlType aFinalstate) {
            switch (aFinalstate) {

                case FINALSTATE:
                    ModelUml.MXUPseudoState lUmlMUNode = fUmlState.addPseudoState(PseudoKind.FINAL, aUUID, aName);
                    return new MUPseudoState(lUmlMUNode, this);
                case PSEUDO_STATE:
                    break;
                case STATE:
                    break;
            }
            return null;
        }

        @Override
        public IMUPseudoState addPseudoState(IId aUUID, String aName, PseudoKind aKind) {
            ModelUml.MXUPseudoState lUmlMUNode = fUmlState.addPseudoState(aKind, aUUID, aName);
            return new MUPseudoState(lUmlMUNode, this);
        }

        @Override
        public IMUState addState(IId aUUID, String aName) {
            ModelUml.MXUSimpleState lUmlMUNode = fUmlState.addState(aUUID, aName);
            return new MUSimpleState(lUmlMUNode, this);
        }

        @Override
        public IMUStateMachine addSubMachine(IId aUUID, String aName) {
            ModelUml.MXUStateMachineState lUmlState = fUmlState.addSubMachine(aUUID, aName);
            return new MUStateMachine(lUmlState, this);
        }

        @Override
        public IMUTransition addTransition(IMUState aSourceState, IMUState aTargetState, TransitionKind aKind, IMUTrigger aTrigger) {
            IMUTransition lTransition = new MUTransition(
                    this,
                    fUmlState.addTransition(
                            aSourceState.getId(),
                            aTargetState.getId(),
                            aTrigger != null ? ((MUTrigger) aTrigger).getMUTrigger() : null
                    )
            );
            String lTransitionName = aSourceState.getName() + "__" + aTargetState.getName();
            if (aTrigger != null) {
                lTransitionName += "#" + aTrigger.getName();
            }
            lTransition.setName(lTransitionName);

            return lTransition;
        }

        @Override
        public IMUTransition addTransition(IMUState aSourceState, IMUState aTargetState, TransitionKind aKind, String aConnectionName) {
            IMUTransition lTransition = new MUTransition(
                    this,
                    fUmlState.addTransition(
                            aSourceState.getId(),
                            aTargetState.getId(),
                            null
                    )
            );
            String lTransitionName = aSourceState.getName() + "__" + aTargetState.getName();
            lTransitionName += "#" + aConnectionName;
            lTransition.setName(lTransitionName);

            return lTransition;
        }

        @Override
        public IMURegionState setName(String aName) {
            fUmlState.setName(aName);
            return this;
        }
    }

    class MURegionState extends MURegionMachineSharedState<ModelUml.MXURegionState, MURegionMachineSharedState> implements IMURegionState {

        MURegionState(ModelUml.MXURegionState aUmlState, MURegionMachineSharedState aParent) {
            super(aUmlState, aParent);
        }
    }

    class MUSimpleState extends MUState<ModelUml.MXUStateBase, MURegionMachineSharedState> implements IMUState {

        public MUSimpleState(ModelUml.MXUStateBase aUmlMXUNode, MURegionMachineSharedState aParent) {
            super(aUmlMXUNode, aParent);
        }
    }

    abstract class MUState<UMLMODEL extends ModelUml.MXUStateBase, PARENT extends MUState> extends MUNode<UMLMODEL> {
        PARENT fParent;

        MUState(UMLMODEL aUmlState, PARENT aParent) {
            super(aUmlState);
            fParent = aParent;
            fStateMap.put(aUmlState.getXmiId(), this);
        }

        public IMUAction addEntryAction(String aName) {
            return new MUActionEntry(fUmlState.addEntryAction(aName), aName, this);
        }

        public IMUAction addExitAction(String aName) {
            return new MUActionExit(fUmlState.addExitAction(aName), aName, this);
        }
    }

    class MUStateMachine extends MURegionMachineSharedState<ModelUml.MXUStateMachineState, MURegionMachineSharedState> implements IMUStateMachine {

        public MUStateMachine(ModelUml.MXUStateMachineState aUmlState, MURegionMachineSharedState aParent) {
            super(aUmlState, aParent);
        }

        @Override
        public IMURegionState addRegion(IId aUUID, String aName) {
            ModelUml.MXURegionState lUmlState = fUmlState.addRegionState(aUUID, aName);
            return new MURegionState(lUmlState, this);
        }
    }

    class MUTransition extends MUNode<ModelUml.MXUTransition> implements IMUTransition {
        IMURegionState fParent;

        public MUTransition(IMURegionState aParent, ModelUml.MXUTransition aMUTransition) {
            super(aMUTransition);
            fParent = aParent;
        }

        @Override
        public IMUAction addAction(String aName) {
            return new MUAction(fUmlState.addAction(aName), aName, this);
        }

        @Override
        public IId getId() {
            return fUmlState.getXmiId();
        }

        @Override
        public String getName() {
            return fUmlState.getName();
        }

        @Override
        public IMUGuard setGuard(IId aGuardId) {
            ModelUml.MXUGuard lGuard = fUml.findGuard(aGuardId);
            if (lGuard == null) {
                lGuard = fUml.createGuard(aGuardId);
            }
            fUmlState.setGuard(lGuard);
            IMUGuard lIMUGuard = new MUGuard(lGuard);
            return lIMUGuard;
        }

        @Override
        public IMUTransition setName(String aName) {
            fUmlState.setName(aName);
            return this;
        }

        @Override
        public IMUTransition setSource(IMUState aSource) {
            fUmlState.setSource(aSource.getId());
            return this;
        }

        @Override
        public IMUTransition setTarget(IMUState aTarget) {
            fUmlState.setTarget(aTarget.getId());
            return this;
        }
    }

    class MUTrigger extends MUNode<ModelUml.MXUTrigger> implements IMUTrigger {

        public MUTrigger(String aEvent, Type aType) {
            super(null);
            IId lId;
            try {
                lId = new Sha1Id(aEvent);
            } catch (NoSuchAlgorithmException | UnsupportedEncodingException aE) {
                lId = new UuidId();
            }
            fUmlState = fUml.addTrigger(lId, aEvent, aType);
            fTriggerMap.put(aEvent, this);
        }

        ModelUml.MXUTrigger getMUTrigger() {
            return fUmlState;
        }
    }

    HashMap<IId, MUState>      fStateMap   = new HashMap<>();
    HashMap<String, MUTrigger> fTriggerMap = new HashMap<>();
    ModelUml fUml;
    private MUStateMachine fRootState;

    public MdtUml2Model() throws ParserConfigurationException {
        fUml = new ModelUml();
    }

    @Override
    public IMUTrigger addTrigger(String aEvent, IMUTrigger.Type aType) {
        if (fTriggerMap.containsKey(aEvent))
            return fTriggerMap.get(aEvent);
        return new MUTrigger(aEvent, aType);
    }

    @Override
    public String asString() {
        return fUml.asString();
    }

    @Override
    public IMUState find(IId aSource) {
        return (IMUState) fStateMap.get(aSource);
    }

    @Override
    public IMUStateMachine getRootState(String aName) {
        if (fRootState == null) {
            fRootState = new MUStateMachine(fUml.getRootState(), null);
            fStateMap.put(fRootState.getId(), fRootState);
        }
        return fRootState;
    }

    @Override
    public void save(File aFilename) throws IOException {
        fUml.save(aFilename);
    }
}
