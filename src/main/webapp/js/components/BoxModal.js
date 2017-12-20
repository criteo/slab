// @flow
import { Component } from 'react';
import createFragment from 'react-addons-create-fragment';
import marked from 'marked';
import Modal from 'react-modal';
import type { Box } from '../state';
import CheckList from './CheckList';

marked.setOptions({
  renderer: new marked.Renderer(),
  gfm: true,
  tables: true,
  breaks: true,
  pedantic: false,
  sanitize: false,
  smartLists: true,
  smartypants: false
});

type Props = {
  isOpen: boolean,
  box: Box,
  onCloseClick: Function
};

const style = {
  content: {
    top: '15%',
    bottom: '15%',
    left: '10%',
    right: '10%',
    padding: '0',
    overflow: 'hidden',
    boxShadow: '0 0 24px rgba(0,0,0,.5)',
    border: 'none',
    borderRadius: '0'
  },
  overlay: {
    background: 'rgba(255,255,255,0.25)'
  }
};

class BoxModal extends Component {
  render() {
    const { isOpen, box, onCloseClick } = this.props;
    return (
      <Modal
        isOpen={isOpen}
        style={style}
        contentLabel="box modal"
        closeTimeoutMS={200}
        shouldCloseOnOverlayClick={true}
        onRequestClose={ onCloseClick }
      >
        <div className="box-modal">
          <header className={`${box.status} background`}>
            <span>{box.title}</span>
            <button onClick={onCloseClick}>&times;</button>
          </header>
          <main>
            {
              box.message &&
              createFragment({
                title: <h3><i className="fa fa-sticky-note-o"></i>Message</h3>,
                content: <div className="message" dangerouslySetInnerHTML={{ __html: marked(box.message) }} />
              })
            }
            {
              box.description &&
              createFragment({
                title: <h3><i className="fa fa-file-text-o"></i>Description</h3>,
                content: <div className="description" dangerouslySetInnerHTML={{ __html: marked(box.description) }} />
              })
            }
            <h3><i className="fa fa-list-ol"></i>Checks</h3>
            {
              isOpen ?
              <CheckList
                boxTitle={box.title}
                checks={box.checks}
              /> : null
            }
          </main>
        </div>
      </Modal>
    );
  }

  shouldComponentUpdate(nextProps: Props) {
    // prevent #app from scaling back after each update, as such update will remove .ReactModal__Body--open class from body
    if (this.props.isOpen === nextProps.isOpen)
      return false;
    return true;
  }
}

export default BoxModal;