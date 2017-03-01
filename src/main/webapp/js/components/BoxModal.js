// @flow
import Modal from 'react-modal';
import type { Box, Check } from '../state';
import marked from 'marked';

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
  onCloseClick: Function,
  box: Box
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

const BoxModal = ( { isOpen, box, onCloseClick }: Props) => (
  <Modal
    isOpen={isOpen}
    style={style}
    contentLabel="box modal"
    closeTimeoutMS={200}
  >
    <div className="box-modal">
      <header>
        <span>{box.title}</span>
        <button onClick={onCloseClick}>&times;</button>
      </header>
      <main>
        <section className="info">
          <div className="status">
            <span className={`background circle ${box.status}`}></span>
            <span className={`color ${box.status}`}>{box.status}</span>
            <div className="message">
              { box.message }
            </div>
          </div>
          <div className="description" dangerouslySetInnerHTML={ { __html: marked(box.description || 'No description' ) } } />
        </section>
        <h3>Checks</h3>
        <section className="checks">
          {
            box.checks.map(({ title, status, message }: Check) =>
              <div className="check" key={ title }>
                <span className={`status background ${status}`}></span>
                <div className="content">
                  <h4>{title}</h4>
                  {message}
                </div>
              </div>
            )
          }
        </section>
      </main>
    </div>
  </Modal>
);

export default BoxModal;