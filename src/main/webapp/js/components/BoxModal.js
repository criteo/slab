// @flow
import Modal from 'react-modal';
import type { Box, Check } from '../state';

type Props = {
  isOpen: boolean,
  onCloseClick: Function,
  box: Box
};

const style = {
  content: {
    top: '50%',
    bottom: 'auto',
    left: '50%',
    right: 'auto',
    marginRight: '-50%',
    transform: 'translate(-50%, -50%)',
    padding: '0',
    boxShadow: '0 0 24px rgba(0,0,0,.5)'
  }
};

const BoxModal = ( { isOpen, box, onCloseClick }: Props) => (
  <Modal
    isOpen={isOpen}
    style={style}
    contentLabel="Box modal"
  >
    <div className={`box-modal ${box.status}`}>
      <header>
        <button onClick={onCloseClick}>&times;</button>
      </header>
      <h3>{box.title}</h3>
      { box.message ? <strong>{ box.message }</strong> : null }
      <div className="checks">
        {
          box.checks.map(({ title, status, message }: Check) => 
            <div className={`check ${status}`}>
              <h3>{title}</h3>
              <strong>{message}</strong>
            </div>
          )
        }
      </div>
    </div>
  </Modal>
);

export default BoxModal;