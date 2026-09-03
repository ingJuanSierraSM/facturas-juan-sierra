import { CurrencyPipe, registerLocaleData } from '@angular/common';
import localeEsCo from '@angular/common/locales/es-CO';

describe('Currency formatting', () => {
  beforeAll(() => {
    registerLocaleData(localeEsCo);
  });

  it('preserves two decimal positions for fractional amounts', () => {
    const formattedValue = new CurrencyPipe('es-CO').transform(
      119000.5,
      'COP',
      'symbol-narrow',
      '1.2-2',
      'es-CO',
    );

    expect(formattedValue?.replace(/[^0-9]/g, '')).toBe('11900050');
  });
});
