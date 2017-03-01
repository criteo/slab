// @flow
import { Component } from 'react';

import { findDOMNode, render } from 'react-dom';

import type { Box, Check } from '../state';
import BoxModal from './BoxModal';

type Props = {
  box: Box
};

type State = {
  isModalOpen: boolean
};

class BoxView extends Component {
  props: Props;
  state: State;
  constructor(props: Props) {
    super(props);
    this.state = {
      isModalOpen: false
    };
  }

  render() {
    const { box } = this.props;
    return (
      <div className={`box ${box.status}`} onClick={this.handleBoxClick}>
        <h3>{ box.title }</h3>
        { box.message ? <strong>{ box.message }</strong> : null }
        <div className="checks">
          {
            box.checks.map((c: Check) =>
              <span className={`check ${c.status}`} key={c.title}>{c.label || c.title}</span>
            )
          }
        </div>
        <div id="more"></div>
        <BoxModal
          isOpen={this.state.isModalOpen}
          box={box}
          onCloseClick={ () => this.setState({ isModalOpen: false }) }
        />
      </div>
    );
  }

  handleBoxClick = () => {
    this.setState({ isModalOpen: true });
  }

  componentDidMount() {
    this.adjustIfOverflow();
    window.addEventListener('resize', this.adjustIfOverflow);
  }

  componentWillUnmount() {
    window.removeEventListener('resize', this.adjustIfOverflow);
  }

  adjustIfOverflow = () => {
    const dom = findDOMNode(this);
    if (dom) {
      const checksContainer = dom.querySelector('.checks');
      if (checksContainer.offsetHeight < checksContainer.scrollHeight) {
        render(<span>...</span>, dom.querySelector('#more'));
      } else {
        render(<span></span>, dom.querySelector('#more'));
      }
    }
  }
}

export default BoxView;
