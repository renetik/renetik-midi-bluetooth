package jp.kshoji.javax.sound.midi.ble;

import android.support.annotation.NonNull;

import jp.kshoji.blemidi.device.MidiOutputDevice;
import jp.kshoji.javax.sound.midi.MetaMessage;
import jp.kshoji.javax.sound.midi.MidiDevice;
import jp.kshoji.javax.sound.midi.MidiDeviceReceiver;
import jp.kshoji.javax.sound.midi.MidiMessage;
import jp.kshoji.javax.sound.midi.ShortMessage;
import jp.kshoji.javax.sound.midi.SysexMessage;

/**
 * {@link jp.kshoji.javax.sound.midi.Receiver} implementation
 *
 * @author K.Shoji
 */
public final class BleMidiReceiver implements MidiDeviceReceiver {
    private final BleMidiDevice bleMidiDevice;

    /**
     * Constructor
     *
     * @param bleMidiDevice the device
     */
    public BleMidiReceiver(@NonNull BleMidiDevice bleMidiDevice) {
        this.bleMidiDevice = bleMidiDevice;
    }

    @NonNull
    @Override
    public MidiDevice getMidiDevice() {
        return bleMidiDevice;
    }

    @Override
    public void send(@NonNull MidiMessage message, long l) {
        MidiOutputDevice outputDevice = bleMidiDevice.getMidiOutputDevice();

        if (outputDevice == null) {
            // already closed
            return;
        }

        if (message instanceof MetaMessage) {
            final MetaMessage metaMessage = (MetaMessage) message;
            byte[] metaMessageData = metaMessage.getData();
            if (metaMessageData.length > 0) {
                switch (metaMessageData[0] & 0xff) {
                    case 0xf1:
                        if (metaMessageData.length > 1) {
                            outputDevice.sendMidiTimeCodeQuarterFrame(metaMessageData[1] & 0x7f);
                        }
                        break;
                    case 0xf2:
                        if (metaMessageData.length > 1) {
                            outputDevice.sendMidiSongPositionPointer(metaMessageData[1] & 0x7f);
                        }
                        break;
                    case 0xf3:
                        if (metaMessageData.length > 1) {
                            outputDevice.sendMidiSongSelect(metaMessageData[1] & 0x7f);
                        }
                        break;
                    case 0xf6:
                        outputDevice.sendMidiTuneRequest();
                        break;
                    case 0xf8:
                        outputDevice.sendMidiTimingClock();
                        break;
                    case 0xfa:
                        outputDevice.sendMidiStart();
                        break;
                    case 0xfb:
                        outputDevice.sendMidiContinue();
                        break;
                    case 0xfc:
                        outputDevice.sendMidiStop();
                        break;
                    case 0xfe:
                        outputDevice.sendMidiActiveSensing();
                        break;
                    case 0xff:
                        outputDevice.sendMidiReset();
                        break;
                }
            }
        } else if (message instanceof SysexMessage) {
            final SysexMessage sysexMessage = (SysexMessage) message;
            outputDevice.sendMidiSystemExclusive(sysexMessage.getData());
        } else if (message instanceof ShortMessage) {
            final ShortMessage shortMessage = (ShortMessage) message;
            switch (shortMessage.getCommand()) {
                case ShortMessage.CHANNEL_PRESSURE:
                    outputDevice.sendMidiChannelAftertouch(shortMessage.getChannel(), shortMessage.getData1());
                    break;
                case ShortMessage.CONTROL_CHANGE:
                    outputDevice.sendMidiControlChange(shortMessage.getChannel(), shortMessage.getData1(), shortMessage.getData2());
                    break;
                case ShortMessage.NOTE_OFF:
                    outputDevice.sendMidiNoteOff(shortMessage.getChannel(), shortMessage.getData1(), shortMessage.getData2());
                    break;
                case ShortMessage.NOTE_ON:
                    outputDevice.sendMidiNoteOn(shortMessage.getChannel(), shortMessage.getData1(), shortMessage.getData2());
                    break;
                case ShortMessage.PITCH_BEND:
                    outputDevice.sendMidiPitchWheel(shortMessage.getChannel(), shortMessage.getData1() | (shortMessage.getData2() << 7));
                    break;
                case ShortMessage.POLY_PRESSURE:
                    outputDevice.sendMidiPolyphonicAftertouch(shortMessage.getChannel(), shortMessage.getData1(), shortMessage.getData2());
                    break;
                case ShortMessage.PROGRAM_CHANGE:
                    outputDevice.sendMidiProgramChange(shortMessage.getChannel(), shortMessage.getData1());
                    break;
                default:
            }
        }
    }

    public void open() {
        // do nothing
    }

    @Override
    public void close() {
        // do nothing
    }
}
