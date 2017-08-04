import { showStatsPercentage } from 'src/components/Calendar';

describe('api utils specs', () => {
  it('showStatsPercentage() returns N/A if the base is zero', () => {
    const res = showStatsPercentage({
      successes: 0,
      warnings: 0,
      errors: 0,
      unknown: 10,
      total: 10
    });
    expect(res).to.equal('N/A');
  });
  it('showStatsPercentage() returns the percentage', () => {
    const res = showStatsPercentage({
      successes: 10,
      warnings: 10,
      errors: 10,
      unknown: 10,
      total: 40
    });
    expect(res).to.equal('66.67%');
  });
});