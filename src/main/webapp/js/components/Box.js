// @flow
import { Component } from 'react';

import type { Box, Check } from '../state';
import BoxModal from './BoxModal';

type Props = {
  box: Box,
  onMouseEnter: () => void,
  onMouseLeave: () => void
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
      <div
        className={`box ${box.status} background`}
        onClick={this.handleBoxClick}
        onMouseEnter={this.props.onMouseEnter}
        onMouseLeave={this.props.onMouseLeave}
      >
        <h3>{ box.title }</h3>
        { box.message ? <strong>{ box.message }</strong> : null }
        <div className="checks">
          {
            box.checks && box.checks.slice(0, box.labelLimit).map((c: Check) =>
              <span className={`check ${c.status} background`} key={c.title}>{c.label || c.title}</span>
            )
          }
        </div>
        {
          box.checks && box.labelLimit !== 0 && box.labelLimit < box.checks.length &&
          <div id="more">...</div>
        }
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
}

export default BoxView;
